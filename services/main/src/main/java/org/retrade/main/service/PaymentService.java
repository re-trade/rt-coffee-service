package org.retrade.main.service;

import jakarta.servlet.http.HttpServletRequest;
import org.retrade.main.model.dto.request.PaymentInitRequest;
import org.retrade.main.model.other.PaymentProviderCallbackWrapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PaymentService {
    Optional<String> initPayment(PaymentInitRequest paymentInitRequest, HttpServletRequest httpServletRequest);

    @Transactional
    PaymentProviderCallbackWrapper handlePaymentCallback(HttpServletRequest request, String methodCode);
}
