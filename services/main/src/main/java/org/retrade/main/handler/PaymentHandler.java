package org.retrade.main.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.retrade.main.model.other.PaymentAPICallback;

public interface PaymentHandler {
    String initPayment(int totalAmount, String orderInfo, String returnUri, Long orderId,
                       HttpServletRequest request);
    PaymentAPICallback capturePayment(HttpServletRequest request);
    PaymentAPICallback captureWebhook(HttpServletRequest request);
}
