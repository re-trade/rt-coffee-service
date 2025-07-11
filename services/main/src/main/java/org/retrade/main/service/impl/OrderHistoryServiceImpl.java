package org.retrade.main.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderHistoryServiceImpl implements OrderHistoryService {
    private final OrderHistoryRepository  orderHistoryRepository;
    private final OrderComboRepository orderComboRepository;
    private final OrderStatusRepository orderStatusRepository;
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
                ()-> new ValidationException("Order status not found")
        );

        OrderHistoryEntity orderHistoryEntity = new OrderHistoryEntity();
        orderHistoryEntity.setOrderCombo(orderCombo.get());
        orderHistoryEntity.setSeller(seller);
        orderHistoryEntity.setNotes(request.getNotes());
        orderHistoryEntity.setNewOrderStatus(orderNewStatus);
        orderHistoryEntity.setOldOrderStatus(orderCombo.get().getOrderStatus());
        orderHistoryEntity.setStatus(true);
        try {
            orderHistoryRepository.save(orderHistoryEntity);
            return mapEntityToResponse(orderHistoryEntity);
        }catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }


    }

    @Override
    public OrderHistoryResponse updateOrderHistory(String id) {
        return null;
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
}
