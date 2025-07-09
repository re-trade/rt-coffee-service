package org.retrade.main.service;

import jakarta.servlet.http.HttpServletRequest;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.request.PaymentInitRequest;
import org.retrade.main.model.dto.response.PaymentHistoryResponse;
import org.retrade.main.model.dto.response.PaymentMethodResponse;
import org.retrade.main.model.dto.response.PaymentOrderStatusResponse;
import org.retrade.main.model.dto.response.ProductResponse;
import org.retrade.main.model.other.PaymentProviderCallbackWrapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    Optional<String> initPayment(PaymentInitRequest paymentInitRequest, HttpServletRequest httpServletRequest);

    @Transactional
    PaymentProviderCallbackWrapper handlePaymentCallback(HttpServletRequest request, String methodCode);

    PaginationWrapper<List<PaymentMethodResponse>> getPaymentMethods (QueryWrapper queryWrapper);

//    List<PaymentHistoryResponse> getPaymentHistoriesByCustomerId(String customerId);
//
//    List<PaymentHistoryResponse> getPaymentHistoriesByCurrentCustomer();

    PaginationWrapper<List<PaymentHistoryResponse>> getPaymentHistoriesByCustomerId(String customerId, QueryWrapper queryWrapper);

    PaginationWrapper<List<PaymentHistoryResponse>> getPaymentHistoriesByCurrentCustomer(QueryWrapper queryWrapper);


    PaymentOrderStatusResponse checkOrderPaymentStatusByOrderComboId(String orderComboId);
}
