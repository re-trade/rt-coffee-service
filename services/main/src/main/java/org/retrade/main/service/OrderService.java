package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.request.CancelOrderRequest;
import org.retrade.main.model.dto.request.CreateOrderRequest;
import org.retrade.main.model.dto.response.*;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(String orderId);

    List<OrderResponse> getOrdersByCustomer(String customerId);

    PaginationWrapper<List<CustomerOrderComboResponse>> getCustomerOrderCombos(QueryWrapper queryWrapper);

    PaginationWrapper<List<OrderResponse>> getAllOrders(QueryWrapper queryWrapper);

    OrderResponse updateOrderStatus(String orderId, String statusCode, String notes);

    void cancelOrder(String orderId, String reason);

    void cancelOrderSeller(CancelOrderRequest request);

    void confirmDelivery(String id);

    void completedOrder(String id);

    PaginationWrapper<List<CustomerOrderComboResponse>> getSellerOrderCombos(QueryWrapper queryFieldWrapper);

    CustomerOrderComboResponse getSellerOrderComboById(String comboId);

    CustomerOrderComboResponse getCustomerOrderComboById(String comboId);

    List<OrderResponse> getOrdersByCurrentCustomer();

    PaginationWrapper<List<TopSellersResponse>> getTopSellers(QueryWrapper queryWrapper);

    PaginationWrapper<List<TopCustomerResponse>> getTopCustomerBySeller(QueryWrapper queryWrapper);

    PaginationWrapper<List<SellerOrderComboResponse>>  getAllOrderCombosBySeller(QueryWrapper queryWrapper, String orderStatus);

    PaginationWrapper<List<CustomerOrderComboResponse>> getOrderComboCustomerCanReport(QueryWrapper queryWrapper);

    PaginationWrapper<List<OrderStatusResponse>> getOrderStatusesTemplate(QueryWrapper queryWrapper);

    void cancelOrderCustomer(CancelOrderRequest request);

    OrderStatsResponse getStatsOrderCustomer();
}
