package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.DeliveryTypeEnum;
import org.retrade.main.model.dto.request.DeliveryTrackRequest;
import org.retrade.main.model.dto.response.DeliveryResponse;
import org.retrade.main.model.entity.OrderComboDeliveryEntity;
import org.retrade.main.model.entity.OrderComboEntity;
import org.retrade.main.model.entity.OrderHistoryEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.retrade.main.repository.OrderComboDeliveryRepository;
import org.retrade.main.repository.OrderComboRepository;
import org.retrade.main.repository.OrderHistoryRepository;
import org.retrade.main.service.DeliveryService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {
    private final MessageProducerServiceImpl messageProducerService;
    private final OrderComboDeliveryRepository orderComboDeliveryRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderComboRepository orderComboRepository;
    private final AuthUtils authUtils;

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
        if (!orderCombo.getOrderStatus().getCode().equals("PREPARING")){
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
            addOrderLogs(orderCombo, seller, "Start Delivering");
            return wrapDeliveryResponse(result);
        }catch (Exception ex) {
            throw new ActionFailedException("Have a problem when sign delivery", ex);
        }
    }

    private void addOrderLogs(OrderComboEntity orderCombo, SellerEntity seller, String note) {
        var orderLogs = OrderHistoryEntity.builder()
                .orderCombo(orderCombo)
                .seller(seller)
                .status(true)
                .notes(note)
                .createdBy(seller.getShopName())
                .build();
        try {
            orderHistoryRepository.save(orderLogs);
        } catch (Exception ex) {
            throw new ActionFailedException("Have a problem when add order logs", ex);
        }
    }

    private DeliveryResponse wrapDeliveryResponse (OrderComboDeliveryEntity deliveryEntity) {
        return DeliveryResponse.builder()
                .orderComboId(deliveryEntity.getOrderCombo().getId())
                .deliveryCode(deliveryEntity.getDeliveryCode())
                .deliveryType(deliveryEntity.getDeliveryType())
                .build();
    }
}
