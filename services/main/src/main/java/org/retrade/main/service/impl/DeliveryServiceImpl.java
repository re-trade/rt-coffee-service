package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.DeliveryTypeEnum;
import org.retrade.main.model.constant.OrderStatusCodes;
import org.retrade.main.model.dto.request.DeliveryTrackRequest;
import org.retrade.main.model.dto.response.DeliveryResponse;
import org.retrade.main.model.entity.OrderComboDeliveryEntity;
import org.retrade.main.repository.jpa.OrderComboDeliveryRepository;
import org.retrade.main.repository.jpa.OrderComboRepository;
import org.retrade.main.service.DeliveryService;
import org.retrade.main.util.AuthUtils;
import org.retrade.main.validator.OrderStatusValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {
    private final OrderComboDeliveryRepository orderComboDeliveryRepository;
    private final OrderComboRepository orderComboRepository;
    private final AuthUtils authUtils;
    private final OrderStatusValidator orderStatusValidator;

    @Override
    public DeliveryResponse signDelivery(DeliveryTrackRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        var seller = account.getSeller();
        if (seller == null) {
            throw new ValidationException("Account is not a seller");
        }
        var orderCombo = orderComboRepository.findById(request.getOrderComboId()).orElseThrow(() -> new ValidationException("Not found order combo with this id"));
        if (!orderCombo.getSeller().getId().equals(seller.getId())) {
            throw new ValidationException("Account is not a seller of this order combo");
        }
        if (!orderCombo.getOrderStatus().getCode().equals(OrderStatusCodes.PREPARING)){
            throw new ValidationException("Order combo is not in PREPARING status");
        }
        OrderComboDeliveryEntity.OrderComboDeliveryEntityBuilder builder = OrderComboDeliveryEntity.builder();
        builder.orderCombo(orderCombo);
        builder.deliveryType(request.getDeliveryType());
        if (request.getDeliveryType() == DeliveryTypeEnum.MANUAL || request.getDeliveryCode() == null) {
            request.setDeliveryCode("");
        }
        builder.deliveryCode(request.getDeliveryCode());
        try {
            var result = orderComboDeliveryRepository.save(builder.build());
            return wrapDeliveryResponse(result);
        }catch (Exception ex) {
            throw new ActionFailedException("Have a problem when sign delivery", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryByOrderComboId(String orderComboId) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.getCustomer() == null) {
            throw new ValidationException("Account is not a customer");
        }
        var customer = account.getCustomer();
        var orderCombo = orderComboRepository.findById(orderComboId).orElseThrow(() -> new ValidationException("Order combo not found"));
        if (!Set.of(OrderStatusCodes.PENDING, OrderStatusCodes.PREPARING).contains(orderCombo.getOrderStatus().getCode())) {
            throw new ValidationException("Order combo is not in PENDING or PREPARING status");
        }
        if (!orderCombo.getOrderDestination().getOrder().getCustomer().getId().equals(customer.getId())) {
            throw new ValidationException("You are not allowed to view this order's delivery info");
        }

        if (!orderStatusValidator.isOrderOnDeliveryStatus(orderCombo.getOrderStatus().getCode())) {
            throw new ValidationException("Order is not in a delivery stage");
        }

        var deliveryStatus = orderComboDeliveryRepository.findByOrderCombo(orderCombo);

        if (deliveryStatus.isEmpty()) {
            throw new ValidationException("Delivery status not found");
        }
        OrderComboDeliveryEntity latestDelivery = deliveryStatus.stream()
                .max(Comparator.comparing(OrderComboDeliveryEntity::getCreatedDate))
                .orElseThrow(() -> new ValidationException("No delivery info found"));
        return wrapDeliveryResponse(latestDelivery);
    }

    private DeliveryResponse wrapDeliveryResponse (OrderComboDeliveryEntity deliveryEntity) {
        return DeliveryResponse.builder()
                .orderComboId(deliveryEntity.getOrderCombo().getId())
                .deliveryCode(deliveryEntity.getDeliveryCode())
                .deliveryType(deliveryEntity.getDeliveryType())
                .build();
    }
}
