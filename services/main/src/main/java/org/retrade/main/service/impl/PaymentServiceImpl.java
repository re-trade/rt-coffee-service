package org.retrade.main.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.handler.PaymentHandler;
import org.retrade.main.model.constant.PaymentStatusEnum;
import org.retrade.main.model.dto.request.PaymentInitRequest;
import org.retrade.main.model.entity.PaymentHistoryEntity;
import org.retrade.main.model.entity.PaymentMethodEntity;
import org.retrade.main.model.other.PaymentProviderCallbackWrapper;
import org.retrade.main.repository.*;
import org.retrade.main.service.PaymentService;
import org.retrade.main.util.RandomUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final ApplicationContext applicationContext;
    private final OrderRepository orderRepository;
    private final OrderComboRepository orderComboRepository;
    @Value("${payment.callback}")
    private String callbackUrl;

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, ValidationException.class, Exception.class})
    public Optional<String> initPayment(PaymentInitRequest paymentInitRequest, HttpServletRequest httpServletRequest) {
        var orderEntity = orderRepository.findById(paymentInitRequest.getOrderId()).orElseThrow(() -> new ValidationException("Order not found"));
        var paymentMethodEntity = paymentMethodRepository.findById(paymentInitRequest.getPaymentMethodId()).orElseThrow(() -> new ValidationException("Payment method not found"));
        var paymentCode = RandomUtils.generatePaymentCode();
        var payment = PaymentHistoryEntity.builder()
                .order(orderEntity)
                .paymentCode(String.valueOf(paymentCode))
                .paymentContent(paymentInitRequest.getPaymentContent())
                .paymentMethod(paymentMethodEntity)
                .paymentTotal(orderEntity.getGrandTotal())
                .paymentStatus(PaymentStatusEnum.CREATED)
                .build();
        try {
            paymentHistoryRepository.save(payment);
        } catch (Exception ex) {
            throw new ValidationException("Have a problem when init payment", ex);
        }
        var paymentHandler = getPaymentHandler(paymentInitRequest.getPaymentMethodId());
        if (paymentHandler.isPresent()) {
            var paymentLink =  paymentHandler.get().initPayment(
                    orderEntity.getGrandTotal().intValue(),
                    paymentInitRequest.getPaymentContent(),
                    paymentMethodEntity.getCallbackUri(),
                    (long) paymentCode,
                    httpServletRequest
            );
            return Optional.of(paymentLink);
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    @Override
    public PaymentProviderCallbackWrapper handlePaymentCallback(HttpServletRequest request, String methodCode) {
        try {
            var paymentHandler = getPaymentHandler(methodCode.toUpperCase())
                    .orElseThrow(() -> new ValidationException("This payment method is currently not supported"));

            var paymentCallback = paymentHandler.capturePayment(request);
            var paymentEntity = paymentHistoryRepository.findByPaymentCode(String.valueOf(paymentCallback.getId()))
                    .orElseThrow(() -> new ValidationException("Not found payment with this id"));
            paymentEntity.setPaymentTime(LocalDateTime.now());
            var order = paymentEntity.getOrder();
            if (paymentCallback.isStatus()) {
                paymentEntity.setPaymentStatus(PaymentStatusEnum.PAID);
                var orderStatus = orderStatusRepository.findByCode("WAIT_FOR_CONFIRMATION")
                        .orElseThrow(() -> new ValidationException("Not found order status"));
                var orderCombos = orderComboRepository.findByOrderItems_Order_Id(order.getId());
                orderCombos.forEach(orderCombo -> {
                    orderCombo.setOrderStatus(orderStatus);
                });
                orderComboRepository.saveAll(orderCombos);
            } else {
                paymentEntity.setPaymentStatus(PaymentStatusEnum.CANCELED);
            }
            try {
                paymentHistoryRepository.save(paymentEntity);
                orderRepository.save(order);
            } catch (Exception ex) {
                throw new ActionFailedException("Failed to update payment/order: " + ex.getMessage());
            }
            if (paymentCallback.isStatus()) {
                return handleSuccessCallback(methodCode);
            }
            return handleErrorCallback(methodCode, "Payment has been cancelled");
        } catch (ValidationException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return handleErrorCallback(methodCode, e.getMessage());
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return handleErrorCallback(methodCode, "Something went wrong with our third-party payment method");
        }
    }

    private Optional<PaymentHandler> getPaymentHandler(String code) {
        var paymentMethod = paymentMethodRepository.findByCodeIgnoreCase(code).orElseThrow(() -> new ValidationException("This payment method does not exist"));
        return getPaymentHandler(paymentMethod);
    }

    private Optional<PaymentHandler> getPaymentHandler(PaymentMethodEntity paymentMethodEntity) {
        try {
            String handlerClassName = paymentMethodEntity.getHandlerClass();
            if (handlerClassName != null && !handlerClassName.isEmpty()) {
                return Optional.of((PaymentHandler) applicationContext.getBean(handlerClassName));
            }
            return Optional.empty();
        } catch (Exception ex) {
            throw new ValidationException("Payment method does not support");
        }
    }

    private PaymentProviderCallbackWrapper handleSuccessCallback(String methodCode) {
        return PaymentProviderCallbackWrapper.builder()
                .callbackUrl(String.format("%s?status=%s&method=%s", callbackUrl, true, methodCode))
                .message("Payment successful")
                .success(true)
                .build();
    }

    private PaymentProviderCallbackWrapper handleErrorCallback(String methodCode, String message) {
        return PaymentProviderCallbackWrapper.builder()
                .callbackUrl(String.format("%s?status=%s&method=%s", callbackUrl, false, methodCode))
                .message(message)
                .success(false)
                .build();
    }
}
