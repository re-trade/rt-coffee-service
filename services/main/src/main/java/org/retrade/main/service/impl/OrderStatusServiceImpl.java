package org.retrade.main.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.response.OrderStatusResponse;
import org.retrade.main.model.entity.OrderComboEntity;
import org.retrade.main.model.entity.OrderStatusEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.retrade.main.repository.jpa.OrderComboRepository;
import org.retrade.main.repository.jpa.OrderStatusRepository;
import org.retrade.main.service.OrderStatusService;
import org.retrade.main.util.AuthUtils;
import org.retrade.main.util.OrderStatusValidator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderStatusServiceImpl implements OrderStatusService {
    private final OrderStatusRepository orderStatusRepository;
    private final OrderStatusValidator orderStatusValidator;
    private final OrderComboRepository orderComboRepository;
    private final AuthUtils authUtils;

    @Override
    public List<OrderStatusResponse> getAllStatusTrue() {
        List<OrderStatusEntity> orderStatusEntities = orderStatusRepository.findAllByEnabledTrue();
        return orderStatusEntities.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderStatusResponse> getAllStatusTrueForSellerChange() {
        List<OrderStatusEntity> orderStatusEntities = orderStatusRepository.findAllByEnabledTrue();
        return orderStatusEntities.stream()
                .filter(status -> List.of(
                        "PENDING",
                        "PREPARING",
                        "DELIVERED",
                        "DELIVERING",
                        "CONFIRMED",
                        "COMPLETED").contains(status.getCode()))
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderStatusResponse> getAllStatusNextStep(String orderComboId) {

        SellerEntity sellerEntity = getSellerEntity();

        Optional<OrderComboEntity> orderComboEntity = orderComboRepository.findByIdAndSeller(orderComboId, sellerEntity);
        if (orderComboEntity.isEmpty()) {
            throw new ValidationException("Order combo not found for ID: " + orderComboId);
        }

        String currentStatus = orderComboEntity.get().getOrderStatus().getCode();

        Set<String> nextStatusCodes = orderStatusValidator.getValidNextStatuses(currentStatus);

        List<OrderStatusResponse> responses = new ArrayList<>();

        for (String statusCode : nextStatusCodes) {
            Optional<OrderStatusEntity> statusEntity = orderStatusRepository.findByCode(statusCode);
            if (statusEntity.isEmpty()) {
                throw new ValidationException("Order status not found for code: " + statusCode);
            }
            responses.add(mapEntityToResponse(statusEntity.get()));
        }

        return responses;
    }

    public List<OrderStatusResponse> getAllStatusTrueForRefund() {
        List<OrderStatusEntity> orderStatusEntities = orderStatusRepository.findAllByEnabledTrue();
        return orderStatusEntities.stream()
                .filter(status -> List.of(
                        "REFUNDED",
                        "PAYMENT_CANCELLED",
                        "PAYMENT_FAILED"
                ).contains(status.getCode()))

                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }
    public List<OrderStatusResponse> getAllStatusTrueForReturn() {
        List<OrderStatusEntity> orderStatusEntities = orderStatusRepository.findAllByEnabledTrue();
        return orderStatusEntities.stream()
                .filter(status -> List.of(
                        "RETURNING",
                        "RETURN_REQUESTED",
                        "RETURN_APPROVED",
                        "RETURN_REJECTED",
                        "RETURNED"
                ).contains(status.getCode()))

                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }
    private OrderStatusResponse mapEntityToResponse(OrderStatusEntity orderStatusEntity) {
        return OrderStatusResponse
                .builder()
                .id(orderStatusEntity.getId())
                .code(orderStatusEntity.getCode())
                .name(orderStatusEntity.getName())
                .enabled(orderStatusEntity.getEnabled())
                .build();
    }
    private SellerEntity getSellerEntity() {
        return authUtils.getUserAccountFromAuthentication().getSeller();
    }
}
