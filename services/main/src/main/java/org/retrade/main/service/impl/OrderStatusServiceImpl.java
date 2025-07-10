package org.retrade.main.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.retrade.main.model.dto.response.OrderStatusResponse;
import org.retrade.main.model.entity.OrderStatusEntity;
import org.retrade.main.repository.OrderStatusRepository;
import org.retrade.main.service.OrderStatusService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderStatusServiceImpl implements OrderStatusService {
    private final OrderStatusRepository orderStatusRepository;
    @Override
    public List<OrderStatusResponse> getAllStatusTrue() {
        List<OrderStatusEntity> orderStatusEntities = orderStatusRepository.findAllByEnabledTrue();
        return orderStatusEntities.stream()
                .map(this::maEntityToResponse)
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
                .map(this::maEntityToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderStatusResponse> getAllStatusTrueForRefund() {
        List<OrderStatusEntity> orderStatusEntities = orderStatusRepository.findAllByEnabledTrue();
        return orderStatusEntities.stream()
                .filter(status -> List.of(
                        "REFUNDED",
                        "PAYMENT_CANCELLED",
                        "PAYMENT_FAILED"
                ).contains(status.getCode()))

                .map(this::maEntityToResponse)
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

                .map(this::maEntityToResponse)
                .collect(Collectors.toList());
    }
    private OrderStatusResponse maEntityToResponse(OrderStatusEntity orderStatusEntity) {
        return OrderStatusResponse
                .builder()
                .id(orderStatusEntity.getId())
                .code(orderStatusEntity.getCode())
                .name(orderStatusEntity.getName())
                .enabled(orderStatusEntity.getEnabled())
                .build();
    }
}
