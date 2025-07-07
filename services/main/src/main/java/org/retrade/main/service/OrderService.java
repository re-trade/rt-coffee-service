package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.request.CreateOrderRequest;
import org.retrade.main.model.dto.response.CustomerOrderComboResponse;
import org.retrade.main.model.dto.response.OrderResponse;
import org.retrade.main.model.dto.response.OrderStatusResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderService {
    
    OrderResponse createOrder(CreateOrderRequest request);
    
    OrderResponse getOrderById(String orderId);
    
    List<OrderResponse> getOrdersByCustomer(String customerId);

    PaginationWrapper<List<CustomerOrderComboResponse>> getCustomerOrderCombos(QueryWrapper queryWrapper);

    PaginationWrapper<List<OrderResponse>> getAllOrders(QueryWrapper queryWrapper);
    
    OrderResponse updateOrderStatus(String orderId, String statusCode, String notes);
    
    void cancelOrder(String orderId, String reason);

    PaginationWrapper<List<CustomerOrderComboResponse>> getSellerOrderCombos(QueryWrapper queryFieldWrapper);

    @Transactional(readOnly = true)
    CustomerOrderComboResponse getSellerOrderComboById(String comboId);

    @Transactional(readOnly = true)
    CustomerOrderComboResponse getCustomerOrderComboById(String comboId);

    List<OrderResponse> getOrdersByCurrentCustomer();

    PaginationWrapper<List<CustomerOrderComboResponse>>  getAllOrderCombosBySeller(QueryWrapper queryWrapper);

    PaginationWrapper<List<OrderStatusResponse>> getOrderStatusesTemplate(QueryWrapper queryWrapper);
}
