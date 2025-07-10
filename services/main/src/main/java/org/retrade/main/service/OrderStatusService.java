package org.retrade.main.service;

import org.retrade.main.model.dto.response.OrderStatusResponse;

import java.util.List;

public interface OrderStatusService {

    List<OrderStatusResponse> getAllStatusTrue();

    List<OrderStatusResponse> getAllStatusTrueForSellerChange();
}
