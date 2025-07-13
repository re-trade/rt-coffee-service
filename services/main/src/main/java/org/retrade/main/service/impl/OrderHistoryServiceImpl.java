package org.retrade.main.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.CreateOrderHistoryRequest;
import org.retrade.main.model.dto.response.OrderHistoryResponse;
import org.retrade.main.model.dto.response.OrderStatusResponse;
import org.retrade.main.model.entity.OrderComboEntity;
import org.retrade.main.model.entity.OrderHistoryEntity;
import org.retrade.main.model.entity.OrderStatusEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.retrade.main.repository.OrderComboRepository;
import org.retrade.main.repository.OrderHistoryRepository;
import org.retrade.main.repository.OrderStatusRepository;
import org.retrade.main.repository.SellerRepository;
import org.retrade.main.service.OrderHistoryService;
import org.retrade.main.util.AuthUtils;
import org.retrade.main.util.OrderStatusValidator;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderHistoryServiceImpl implements OrderHistoryService {
    private final OrderHistoryRepository  orderHistoryRepository;
    private final OrderComboRepository orderComboRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderStatusValidator orderStatusValidator;
    private final AuthUtils authUtils;
    @Override
    public List<OrderHistoryResponse> getAllNotesByOrderComboId(String id) {
        List<OrderHistoryEntity> orderHistoryEntityList = orderHistoryRepository.findByOrderCombo_Id(id);
        return orderHistoryEntityList.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderHistoryResponse getDetailsOrderHistory(String id) {
        OrderHistoryEntity orderHistoryEntity = orderHistoryRepository.findById(id).orElseThrow(
                ()-> new ValidationException("Order history not found")
        );
        return mapEntityToResponse(orderHistoryEntity);
    }

    @Transactional
    @Override
    public OrderHistoryResponse createOrderHistory(CreateOrderHistoryRequest request) {
        var seller = getSellerEntity();
        if (seller == null) {
            throw new ValidationException("Seller not found");
        }

        Optional<OrderComboEntity> orderCombo = orderComboRepository.findByIdAndSeller(request.getOrderComboId(), seller);
        if (orderCombo.isEmpty()) {
            throw new ValidationException("This order does not belong to you");
        }

        OrderStatusEntity orderNewStatus = orderStatusRepository.findById(request.getNewStatusId()).orElseThrow(
                () -> new ValidationException("Order status not found")
        );

        if (orderNewStatus.equals(orderCombo.get().getOrderStatus())) {
            throw new ValidationException("Order status is already in use");
        }

        String currentStatusCode = orderCombo.get().getOrderStatus().getCode();
        String newStatusCode = orderNewStatus.getCode();

        // Validate chuyển đổi status có hợp lệ không
        if (!orderStatusValidator.isValidStatusTransition(currentStatusCode, newStatusCode)) {
            Set<String> validNextStatuses = orderStatusValidator.getValidNextStatuses(currentStatusCode);
            String validStatusesStr = String.join(", ", validNextStatuses);

            throw new ValidationException(
                    String.format("Invalid status transition from %s to %s. Valid transitions: [%s]",
                            currentStatusCode, newStatusCode, validStatusesStr)
            );
        }

        OrderHistoryEntity orderHistoryEntity = new OrderHistoryEntity();
        orderHistoryEntity.setOrderCombo(orderCombo.get());
        orderHistoryEntity.setSeller(seller);
        orderHistoryEntity.setNotes(request.getNotes());
        orderHistoryEntity.setNewOrderStatus(orderNewStatus);
        orderHistoryEntity.setOldOrderStatus(orderCombo.get().getOrderStatus());
        orderHistoryEntity.setStatus(true);

        orderCombo.get().setOrderStatus(orderNewStatus);

        try {
            orderComboRepository.save(orderCombo.get());
            orderHistoryRepository.save(orderHistoryEntity);

            return mapEntityToResponse(orderHistoryEntity);
        } catch (Exception e) {
            throw new ActionFailedException(e.getMessage());
        }
    }

    @Override
    public OrderHistoryResponse updateOrderHistory(String id) {
        var seller = getSellerEntity();
        OrderHistoryEntity orderHistoryEntity = orderHistoryRepository.findByIdAndSeller(id, seller);
        if (orderHistoryEntity == null) {
            throw new ValidationException("Order history not found");
        }
        orderHistoryEntity.setNotes(orderHistoryEntity.getNotes());
        orderHistoryEntity.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
        try {
            orderHistoryRepository.save(orderHistoryEntity);
            return mapEntityToResponse(orderHistoryEntity);
        }catch (Exception e) {
            throw new ActionFailedException(e.getMessage());
        }
    }

    private SellerEntity getSellerEntity() {
       return authUtils.getUserAccountFromAuthentication().getSeller();
    }
    private OrderHistoryResponse mapEntityToResponse(OrderHistoryEntity orderHistoryEntity) {
        return OrderHistoryResponse
                .builder()
                .id(orderHistoryEntity.getId())
                .orderComboId(orderHistoryEntity.getOrderCombo().getId())
                .sellerId(orderHistoryEntity.getSeller().getId())
                .notes(orderHistoryEntity.getNotes())
                .newOrderStatus(mapEntityToResponse(orderHistoryEntity.getNewOrderStatus()))
                .oldOrderStatus(mapEntityToResponse(orderHistoryEntity.getOldOrderStatus()))
                .orderComboId(orderHistoryEntity.getOrderCombo().getId())
                .createdAt(orderHistoryEntity.getCreatedDate().toLocalDateTime())
                .updatedAt(orderHistoryEntity.getUpdatedDate().toLocalDateTime())
                .build();
    }
    private OrderStatusResponse mapEntityToResponse(OrderStatusEntity orderStatusEntity) {
        return OrderStatusResponse
                .builder()
                .id(orderStatusEntity.getId())
                .code(orderStatusEntity.getCode())
                .name(orderStatusEntity.getName())
                .build();
    }
    private boolean validateStatus(String currentStatus, String nextStatus) {
        List<String> validFlow = List.of(
                "PENDING",
                "CONFIRMED",
                "PREPARING",
                "DELIVERING",
                "DELIVERED"
        );

        int currentIndex = validFlow.indexOf(currentStatus);
        int nextIndex = validFlow.indexOf(nextStatus);
        if (currentIndex == -1 || nextIndex == -1) {
            return false;
        }

        return nextIndex == currentIndex + 1;
    }

}
