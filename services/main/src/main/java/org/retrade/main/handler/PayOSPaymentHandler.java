package org.retrade.main.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.main.model.other.PaymentAPICallback;
import org.retrade.main.util.NetUtils;
import org.springframework.stereotype.Component;
import vn.payos.PayOS;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;
import vn.payos.type.Webhook;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Component(value = "org.retrade.main.handler.PayOSPaymentHandler")
@RequiredArgsConstructor
public class PayOSPaymentHandler implements PaymentHandler {
    private final PayOS payOS;
    private final NetUtils netUtils;
    private final ObjectMapper objectMapper;
    @Override
    public String initPayment(int totalAmount, String orderInfo, String returnUri, Long orderId,
                              HttpServletRequest request) {
        var paymentData = PaymentData.builder()
                .orderCode(orderId)
                .amount(totalAmount)
                .description(orderInfo)
                .returnUrl(netUtils.generateCallbackUrl(request, returnUri))
                .cancelUrl(netUtils.generateCallbackUrl(request, returnUri))
                .build();
        try {
            return payOS.createPaymentLink(paymentData).getCheckoutUrl();
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to init payment with payos, please choice other payment method", ex);
        }
    }

    @Override
    public PaymentAPICallback capturePayment(HttpServletRequest request) {
        int orderCode = Integer.parseInt(request.getParameter("orderCode"));
        try {
            PaymentLinkData data = payOS.getPaymentLinkInformation((long) orderCode);
            return PaymentAPICallback.builder()
                    .id((long) orderCode)
                    .status("PAID".equals(data.getStatus()))
                    .orderInfo(data.getStatus())
                    .transactionId(data.getId())
                    .total(new BigDecimal(data.getAmount()))
                    .build();
        } catch (Exception e) {
            return PaymentAPICallback.builder()
                    .id((long) orderCode)
                    .status(false)
                    .build();
        }
    }

    @Override
    public PaymentAPICallback captureWebhook(HttpServletRequest request) {
        try {
            var json = new String (request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            var payload =  objectMapper.readValue(json, new TypeReference<Webhook>() {});
            var result = payOS.verifyPaymentWebhookData(payload);
            return PaymentAPICallback.builder()
                    .id(result.getOrderCode())
                    .status(payload.getSuccess())
                    .orderInfo(result.getDesc())
                    .transactionId(result.getPaymentLinkId())
                    .total(new BigDecimal(result.getAmount()))
                    .build();
        } catch (Exception e) {
            return PaymentAPICallback.builder()
                    .id((long) -1)
                    .status(false)
                    .build();
        }
    }
}
