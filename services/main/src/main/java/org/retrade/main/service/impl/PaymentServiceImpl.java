package org.retrade.main.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.entity.BaseSQLEntity;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.handler.PaymentHandler;
import org.retrade.main.model.constant.PaymentStatusEnum;
import org.retrade.main.model.dto.request.PaymentInitRequest;
import org.retrade.main.model.dto.response.PaymentHistoryResponse;
import org.retrade.main.model.dto.response.PaymentMethodResponse;
import org.retrade.main.model.dto.response.PaymentOrderStatusResponse;
import org.retrade.main.model.entity.*;
import org.retrade.main.model.other.PaymentAPICallback;
import org.retrade.main.model.other.PaymentProviderCallbackWrapper;
import org.retrade.main.repository.jpa.*;
import org.retrade.main.service.PaymentService;
import org.retrade.main.util.AuthUtils;
import org.retrade.main.util.RandomUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final ApplicationContext applicationContext;
    private final OrderRepository orderRepository;
    private final OrderComboRepository orderComboRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    @Value("${payment.callback}")
    private String callbackUrl;
    private final AuthUtils authUtils;
    private final CustomerRepository customerRepository;

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
        var paymentHandler = getPaymentHandler(paymentMethodEntity);
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
            return getPaymentProviderCallbackWrapper(methodCode, paymentCallback);
        } catch (ValidationException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return handleErrorCallback(methodCode, e.getMessage());
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return handleErrorCallback(methodCode, "Something went wrong with our third-party payment method");
        }
    }

    @Transactional
    @Override
    public PaymentProviderCallbackWrapper handleIPNWebhookCallback(HttpServletRequest request, String methodCode) {
        try {
            var paymentHandler = getPaymentHandler(methodCode.toUpperCase())
                    .orElseThrow(() -> new ValidationException("This payment method is currently not supported"));

            var paymentCallback = paymentHandler.captureWebhook(request);
            return getPaymentProviderCallbackWrapper(methodCode, paymentCallback);
        } catch (ValidationException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return handleErrorCallback(methodCode, e.getMessage());
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return handleErrorCallback(methodCode, "Something went wrong with our third-party payment method");
        }
    }

    @Override
    public PaginationWrapper<List<PaymentMethodResponse>> getPaymentMethods(QueryWrapper queryWrapper) {
        return paymentMethodRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::wrapPaymentMethodResponse).stream().toList();
            return new PaginationWrapper.Builder<List<PaymentMethodResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    public PaginationWrapper<List<PaymentHistoryResponse>> getPaymentHistoriesByCustomerId(String customerId, QueryWrapper queryWrapper) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ValidationException("Seller not found"));
        return getPaymentHistoriesByCustomer(customer, queryWrapper);
    }

    @Override
    public PaginationWrapper<List<PaymentHistoryResponse>> getPaymentHistoriesByCurrentCustomer(QueryWrapper queryWrapper) {
        var customer = getCurrentCustomerAccount();

        return getPaymentHistoriesByCustomer(customer, queryWrapper);
    }

    @Override
    public PaymentOrderStatusResponse checkOrderPaymentStatusByOrderComboId(String orderComboId) {
        var account = authUtils.getUserAccountFromAuthentication();
        var customerEntity = account.getCustomer();
        if (customerEntity == null) {
            throw new ValidationException("User is not a customer");
        }
        if (!orderComboRepository.existsById(orderComboId)) {
            throw new ValidationException("Order combo not found");
        }
        var orderEntity = orderRepository.findOrderByOrderComboId(orderComboId).orElseThrow(() -> new ValidationException("Order not found"));
        var paymentHistory = paymentHistoryRepository.findByOrder(orderEntity);

        Map<String, List<PaymentHistoryEntity>> grouped = paymentHistory.stream()
                .collect(Collectors.groupingBy(ph -> switch (ph.getPaymentStatus()) {
                    case CREATED -> "created";
                    case PAID -> "paid";
                    default -> "cancelled";
                }));

        var paidHistories = grouped.getOrDefault("paid", List.of());
        var totalPaid = paidHistories.stream()
                .map(PaymentHistoryEntity::getPaymentTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        boolean isFullyPaid = totalPaid.compareTo(orderEntity.getGrandTotal()) >= 0;
        Set<String> relatedComboIds = orderComboRepository.findByOrderDestination(orderEntity.getOrderDestination()).stream()
                .map(BaseSQLEntity::getId)
                .collect(Collectors.toSet());

        return PaymentOrderStatusResponse.builder()
                .paid(isFullyPaid)
                .orderId(orderEntity.getId())
                .relatedComboIds(relatedComboIds)
                .build();
    }


    private PaginationWrapper<List<PaymentHistoryResponse>> getPaymentHistoriesByCustomer(CustomerEntity customer, QueryWrapper queryWrapper) {
        return paymentHistoryRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("order").get("customer"), customer));
            return getPredicatePaymentHistory(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::mapToPaymentHistoryResponse).stream().toList();
            return new PaginationWrapper.Builder<List<PaymentHistoryResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }


    private Predicate getPredicatePaymentHistory(Map<String, QueryFieldWrapper> param, Root<PaymentHistoryEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = paymentHistoryRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private CustomerEntity getCurrentCustomerAccount() {
        var account = authUtils.getUserAccountFromAuthentication();
        var customerEntity = account.getCustomer();
        if (customerEntity == null) {
            throw new ValidationException("User is not a customer");
        }
        return customerEntity;
    }

    private PaymentHistoryResponse mapToPaymentHistoryResponse(PaymentHistoryEntity payment) {
        return PaymentHistoryResponse.builder()
                .orderId(payment.getOrder().getId())
                .paymentMethodName(payment.getPaymentMethod().getName())
                .paymentTotal(payment.getPaymentTotal())
                .paymentContent(payment.getPaymentContent())
                .paymentCode(payment.getPaymentCode())
                .paymentStatus(payment.getPaymentStatus())
                .paymentTime(payment.getPaymentTime())
                .build();
    }

    private PaymentProviderCallbackWrapper getPaymentProviderCallbackWrapper(String methodCode, PaymentAPICallback paymentCallback) {
        var paymentEntity = paymentHistoryRepository.findByPaymentCode(String.valueOf(paymentCallback.getId()))
                .orElseThrow(() -> new ValidationException("Not found payment with this id"));
        paymentEntity.setPaymentTime(LocalDateTime.now());
        var order = paymentEntity.getOrder();
        if (paymentCallback.isStatus()) {
            paymentEntity.setPaymentStatus(PaymentStatusEnum.PAID);
            var orderStatus = orderStatusRepository.findByCode("PAYMENT_CONFIRMATION")
                    .orElseThrow(() -> new ValidationException("Not found order status"));
            var orderCombos = orderComboRepository.findByOrderItems_Order_Id(order.getId());
            orderCombos.forEach(orderCombo -> {
                orderCombo.setOrderStatus(orderStatus);
            });
            orderComboRepository.saveAll(orderCombos);
        } else {
            paymentEntity.setPaymentStatus(PaymentStatusEnum.CANCELED);
            restoreProductQuantities(order);
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
    }

    private void restoreProductQuantities(OrderEntity order) {
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrder_Id(order.getId());

        for (OrderItemEntity item : orderItems) {
            ProductEntity product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
        }
        productRepository.saveAll(
                orderItems.stream()
                        .map(OrderItemEntity::getProduct)
                        .collect(Collectors.toList())
        );
    }


    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<PaymentMethodEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = paymentMethodRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private PaymentMethodResponse wrapPaymentMethodResponse(PaymentMethodEntity paymentMethodEntity) {
        return PaymentMethodResponse.builder()
                .id(paymentMethodEntity.getId())
                .code(paymentMethodEntity.getCode())
                .name(paymentMethodEntity.getName())
                .type(paymentMethodEntity.getType())
                .imgUrl(paymentMethodEntity.getImgUrl())
                .description(paymentMethodEntity.getDescription())
                .build();
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
