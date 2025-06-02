package org.retrade.main.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.main.model.other.PaymentAPICallback;
import org.retrade.main.util.NetUtils;
import org.springframework.stereotype.Component;
import vn.payos.PayOS;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

import java.math.BigDecimal;

@Component(value = "org.retrade.main.handler.PayOSPaymentHandler")
@RequiredArgsConstructor
public class PayOSPaymentHandler implements PaymentHandler {
    private final PayOS payOS;
    private final NetUtils netUtils;
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
}
