package org.retrade.main.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.retrade.main.config.provider.VNPayConfig;
import org.retrade.main.model.other.PaymentAPICallback;
import org.retrade.main.util.HashUtils;
import org.retrade.main.util.NetUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component(value = "org.retrade.main.handler.VNPayPaymentHandler")
@RequiredArgsConstructor
public class VNPayPaymentHandler implements PaymentHandler {
    private final VNPayConfig vnPayConfig;
    private final HashUtils hashUtils;
    private final NetUtils netUtils;
    @Override
    public String initPayment(int totalAmount, String orderInfo, String returnUri, Long orderId, HttpServletRequest request) {
        String vnpTxnRef = orderId.toString();
        String vnpIpAddr = netUtils.getIpAddress(request);
        String vnpTmnCode = vnPayConfig.getVnpTmnCode();
        String orderType = "order-type";
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(totalAmount * 100));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", orderType);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", netUtils.generateCallbackUrl(request, returnUri));
        vnpParams.put("vnp_IpAddr", vnpIpAddr);
        addTimestamp(vnpParams);

        String queryUrl = buildQueryUrl(vnpParams);
        String vnpSecureHash = hashUtils.hmacSHA512(vnPayConfig.getVnpHashSecret(), queryUrl);
        return vnPayConfig.getVnpPayUrl() + "?" + queryUrl + "&vnp_SecureHash=" + vnpSecureHash;
    }

    private void addTimestamp(Map<String, String> params) {
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        params.put("vnp_ExpireDate", vnp_ExpireDate);
    }

    private String buildQueryUrl(Map<String, String> vnpParams) {
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII))
                        .append('&');
            }
        }
        query.setLength(query.length() - 1);
        return query.toString();
    }

    public int orderReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = null;
            String fieldValue = null;
            fieldName = URLEncoder.encode(params.nextElement(), StandardCharsets.US_ASCII);
            fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                fields.put(fieldName, fieldValue);
            }
        }
        String vnpSecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        String signValue = hashUtils.hashAllFields(fields, vnPayConfig.getVnpHashSecret());
        if (signValue.equals(vnpSecureHash)) {
            return "00".equals(request.getParameter("vnp_TransactionStatus")) ? 1 : 0;
        } else {
            return -1;
        }
    }

    @Override
    public PaymentAPICallback capturePayment(HttpServletRequest request) {
        int paymentStatus = orderReturn(request);
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");
        Long orderId = Long.valueOf(request.getParameter("vnp_TxnRef"));
        return PaymentAPICallback.builder()
                .status(paymentStatus == 1)
                .total(new BigDecimal(totalPrice))
                .id(orderId)
                .transactionId(transactionId)
                .orderInfo(orderInfo)
                .build();
    }

    @Override
    public PaymentAPICallback captureWebhook(HttpServletRequest request) {
        try {
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
                String paramName = params.nextElement();
                String paramValue = request.getParameter(paramName);
                if (paramValue != null && !paramValue.isEmpty()) {
                    fields.put(paramName, paramValue);
                }
            }
            String vnpSecureHash = fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");
            String signValue = hashUtils.hashAllFields(fields, vnPayConfig.getVnpHashSecret());
            if (!signValue.equalsIgnoreCase(vnpSecureHash)) {
                throw new SecurityException("VNPay signature is invalid");
            }
            int status = "00".equals(fields.get("vnp_TransactionStatus")) ? 1 : 0;
            Long orderId = Long.valueOf(fields.get("vnp_TxnRef"));
            BigDecimal amount = new BigDecimal(fields.get("vnp_Amount")).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            String transactionId = fields.get("vnp_TransactionNo");
            String orderInfo = fields.get("vnp_OrderInfo");
            return PaymentAPICallback.builder()
                    .status(status == 1)
                    .total(amount)
                    .id(orderId)
                    .transactionId(transactionId)
                    .orderInfo(orderInfo)
                    .build();

        } catch (Exception ex) {
            return PaymentAPICallback.builder()
                    .status(false)
                    .id(-1L)
                    .build();
        }
    }

}
