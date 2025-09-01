package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.CancelOrderRequest;
import org.retrade.main.model.dto.request.CreateOrderRequest;
import org.retrade.main.model.dto.response.*;
import org.retrade.main.service.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order with the specified products, delivery address, and payment method")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Product or payment method not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        OrderResponse orderResponse = orderService.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseObject.Builder<OrderResponse>()
                        .success(true)
                        .code("ORDER_CREATED")
                        .content(orderResponse)
                        .messages("Đơn hàng đã được tạo thành công")
                        .build());
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieves order details by order ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<OrderResponse>> getOrderById(
            @Parameter(description = "Order ID", required = true)
            @PathVariable String orderId) {

        var orderResponse = orderService.getOrderById(orderId);

        return ResponseEntity.ok(new ResponseObject.Builder<OrderResponse>()
                .success(true)
                .code("SUCCESS")
                .content(orderResponse)
                .messages("Chi tiết đơn hàng đã được lấy thành công")
                .build());
    }

    @GetMapping("/status")
    public ResponseEntity<ResponseObject<List<OrderStatusResponse>>> getOrderStatuses(@PageableDefault Pageable pageable, @RequestParam(name = "q", required = false)  String query) {
        var result =  orderService.getOrderStatusesTemplate(QueryWrapper.builder()
                        .search(query)
                        .wrapSort(pageable)
                .build());
        return ResponseEntity.ok(new ResponseObject.Builder<List<OrderStatusResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Trạng thái đơn hàng đã được lấy thành công")
                .build());
    }

    @GetMapping("customer/{customerId}")
    @Operation(summary = "Get orders by customer", description = "Retrieves all orders for a specific customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<List<OrderResponse>>> getOrdersByCustomer(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable String customerId) {

        List<OrderResponse> orders = orderService.getOrdersByCustomer(customerId);

        return ResponseEntity.ok(new ResponseObject.Builder<List<OrderResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(orders)
                .messages("Chi tiết đơn hàng đã được lấy thành công")
                .build());
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieves all orders with pagination and filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<List<OrderResponse>>> getAllOrders(
            @Parameter(description = "Search query")
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {

        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();

        var result = orderService.getAllOrders(queryWrapper);

        return ResponseEntity.ok(new ResponseObject.Builder<List<OrderResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Chi tiết đơn hàng đã được lấy thành công")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SELLER')")
    @GetMapping("/top-sellers")
    public ResponseEntity<ResponseObject<List<TopSellersResponse>>> getTopSellers(
            @Parameter(description = "Search query")
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {

        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();

        var result = orderService.getTopSellers(queryWrapper);

        return ResponseEntity.ok(new ResponseObject.Builder<List<TopSellersResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Top người bán đã được lấy thành công")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/top-customers-by-seller")
    public ResponseEntity<ResponseObject<List<TopSellersResponse>>> getTopCustomersBySeller(
            @PathVariable String sellerId,
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {

        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();

        var result = orderService.getTopCustomerBySeller(queryWrapper);

        return ResponseEntity.ok(new ResponseObject.Builder<List<TopSellersResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Top khách hàng theo người bán đã được lấy thành công")
                .build());
    }

    @PutMapping("{orderId}/status")
    @Operation(summary = "Update order status", description = "Updates the status of an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status code"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<OrderResponse>> updateOrderStatus(
            @Parameter(description = "Order ID", required = true)
            @PathVariable String orderId,
            @Parameter(description = "New status code", required = true)
            @RequestParam String statusCode,
            @Parameter(description = "Optional notes")
            @RequestParam(required = false) String notes) {

        OrderResponse orderResponse = orderService.updateOrderStatus(orderId, statusCode, notes);

        return ResponseEntity.ok(new ResponseObject.Builder<OrderResponse>()
                .success(true)
                .code("SUCCESS")
                .content(orderResponse)
                .messages("Trạng thái đơn hàng đã được cập nhật thành công")
                .build());
    }

    @DeleteMapping("{orderId}")
    @Operation(summary = "Cancel order", description = "Cancels an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<Void>> cancelOrder(
            @Parameter(description = "Order ID", required = true)
            @PathVariable String orderId,
            @Parameter(description = "Cancellation reason")
            @RequestParam(required = false) String reason) {

        orderService.cancelOrder(orderId, reason);

        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Đơn hàng đã được hủy thành công")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("customer/combo")
    public ResponseEntity<ResponseObject<List<CustomerOrderComboResponse>>> getOrderCombosByCustomer(@RequestParam(required = false, name = "q") String q,
                                                                                                     @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder().search(q).wrapSort(pageable).build();
        var orders = orderService.getCustomerOrderCombos(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<CustomerOrderComboResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(orders)
                .messages("Danh sách đơn hàng đã được lấy thành công")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("customer/combo/can-report")
    public ResponseEntity<ResponseObject<List<CustomerOrderComboResponse>>> getOrderComboByCustomerCanReport(@RequestParam(required = false, name = "q") String q,
                                                                                                     @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(q).wrapSort(pageable).build();
        var orders = orderService.getOrderComboCustomerCanReport(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<CustomerOrderComboResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(orders)
                .messages("Danh sách đơn hàng đã được lấy thành công")
                .build());
    }


    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("customer/combo/{id}")
    public ResponseEntity<ResponseObject<CustomerOrderComboResponse>> getCustomerOrderCombosById(@PathVariable String id) {
        var order = orderService.getCustomerOrderComboById(id);
        return ResponseEntity.ok(new ResponseObject.Builder<CustomerOrderComboResponse>()
                .success(true)
                .code("SUCCESS")
                .content(order)
                .messages("Danh sách đơn hàng đã được lấy thành công")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("seller/combo/{id}")
    public ResponseEntity<ResponseObject<CustomerOrderComboResponse>> getSellerOrderCombosById(@PathVariable String id) {
        var order = orderService.getSellerOrderComboById(id);
        return ResponseEntity.ok(new ResponseObject.Builder<CustomerOrderComboResponse>()
                .success(true)
                .code("SUCCESS")
                .content(order)
                .messages("Danh sách đơn hàng đã được lấy thành công")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("admin/combo/{id}")
    public ResponseEntity<ResponseObject<SellerOrderComboResponse>> getAdminOrderCombosById(@PathVariable String id) {
        var order = orderService.getAdminOrderComboById(id);
        return ResponseEntity.ok(new ResponseObject.Builder<SellerOrderComboResponse>()
                .success(true)
                .code("SUCCESS")
                .content(order)
                .messages("Danh sách đơn hàng đã được lấy thành công")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("seller")
    public ResponseEntity<ResponseObject<List<CustomerOrderComboResponse>>> getOrderCombosBySeller(@RequestParam(required = false, name = "q") String q,
                                                                                                     @PageableDefault Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder().search(q).wrapSort(pageable).build();
        var orders = orderService.getSellerOrderCombos(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<CustomerOrderComboResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(orders)
                .messages("Danh sách đơn hàng đã được lấy thành công")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("seller/combo")
    public ResponseEntity<ResponseObject<List<SellerOrderComboResponse>>> getAllOrderCombosBySeller(
            @Parameter(description = "Search query to filter products") @RequestParam(required = false, name = "q") String search,
            @RequestParam(required = false) String orderStatus,
            @PageableDefault Pageable pageable) {
        var queryWrapper = new QueryWrapper
                .QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();
        var orders = orderService.getAllOrderCombosBySeller(queryWrapper,orderStatus);
        return ResponseEntity.ok(new ResponseObject.Builder<List<SellerOrderComboResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(orders)
                .messages("Danh sách đơn hàng đã được lấy thành công")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("combo")
    public ResponseEntity<ResponseObject<List<SellerOrderComboResponse>>> getAllOrderCombos(
            @Parameter(description = "Search query to filter products") @RequestParam(required = false, name = "q") String search,
            @RequestParam(required = false) String orderStatus,
            @PageableDefault Pageable pageable) {
        var queryWrapper = new QueryWrapper
                .QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();
        var orders = orderService.getAllOrderCombos(queryWrapper,orderStatus);
        return ResponseEntity.ok(new ResponseObject.Builder<List<SellerOrderComboResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(orders)
                .messages("Danh sách đơn hàng đã được lấy thành công")
                .build());
    }

    @PutMapping("combo/customer/cancel")
    @Operation(summary = "Cancel customer order", description = "Cancels an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<Void>> cancelOrderCustomer(@RequestBody CancelOrderRequest request) {
        orderService.cancelOrderCustomer(request);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Đơn hàng đã được hủy thành công")
                .build());
    }

    @PutMapping("combo/{id}/customer/completed")
    @Operation(summary = "Completed customer order", description = "Completed an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<Void>> completedOrder(@PathVariable String id) {
        orderService.completedOrder(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Đơn hàng đã được hoàn tất thành công")
                .build());
    }

    @PutMapping("combo/{id}/customer/delivery-confirmed")
    @Operation(summary = "Completed customer order", description = "Confirm delivery an order")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<Void>> confirmOrderDelivery(@PathVariable String id) {
        orderService.confirmDelivery(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Đơn hàng đã được hoàn tất thành công")
                .build());
    }

    @PutMapping("combo/seller/cancel")
    @Operation(summary = "Cancel seller order", description = "Cancels an order")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<Void>> cancelOrderSeller(@RequestBody CancelOrderRequest request) {
        orderService.cancelOrderCustomer(request);

        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Đơn hàng đã được hủy thành công")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("customer-stats")
    public ResponseEntity<ResponseObject<OrderStatsResponse>> statsOrderCustomer() {
        OrderStatsResponse stats = orderService.getStatsOrderCustomer();
        return ResponseEntity.ok(new ResponseObject.Builder<OrderStatsResponse>()
                .success(true)
                .code("SUCCESS")
                .messages("Thống kê đơn hàng đã được lấy thành công")
                .content(stats)
                .build());
    }
}
