package org.retrade.main.service;

import org.retrade.main.model.dto.request.CreateOrderHistoryRequest;
import org.retrade.main.model.dto.response.OrderHistoryResponse;

import java.util.List;

public interface OrderHistoryService {
    List<OrderHistoryResponse> getAllNotesByOrderComboId(String id);

    OrderHistoryResponse getDetailsOrderHistory(String id);

    OrderHistoryResponse createOrderHistory(CreateOrderHistoryRequest request);

    OrderHistoryResponse updateOrderHistory(String id);
}
