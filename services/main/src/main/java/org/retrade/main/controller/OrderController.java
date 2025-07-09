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
import org.retrade.main.model.dto.request.CreateOrderRequest;
import org.retrade.main.model.dto.response.CustomerOrderComboResponse;
import org.retrade.main.model.dto.response.OrderResponse;
import org.retrade.main.model.dto.response.SellerOrderComboResponse;
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
                        .messages("Order created successfully")
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
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<OrderResponse>> getOrderById(
            @Parameter(description = "Order ID", required = true)
            @PathVariable String orderId) {

        OrderResponse orderResponse = orderService.getOrderById(orderId);

        return ResponseEntity.ok(new ResponseObject.Builder<OrderResponse>()
                .success(true)
                .code("SUCCESS")
                .content(orderResponse)
                .messages("Order retrieved successfully")
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
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<List<OrderResponse>>> getOrdersByCustomer(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable String customerId) {

        List<OrderResponse> orders = orderService.getOrdersByCustomer(customerId);

        return ResponseEntity.ok(new ResponseObject.Builder<List<OrderResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(orders)
                .messages("Orders retrieved successfully")
                .build());
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieves all orders with pagination and filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SELLER')")
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
                .messages("Orders retrieved successfully")
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
                .messages("Order status updated successfully")
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
                .messages("Order cancelled successfully")
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
                .messages("Orders retrieved successfully")
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
                .messages("Orders retrieved successfully")
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
                .messages("Orders retrieved successfully")
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
                .messages("Orders retrieved successfully")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<List<OrderResponse>>> getOrdersByCurrentCustomer() {

        List<OrderResponse> orders = orderService.getOrdersByCurrentCustomer();

        return ResponseEntity.ok(new ResponseObject.Builder<List<OrderResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(orders)
                .messages("Orders retrieved successfully")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("seller/combo")
    public ResponseEntity<ResponseObject<List<SellerOrderComboResponse>>> getAllOrderCombosBySeller(@RequestParam(required = false, name = "q") String q,
                                                                                                                       @PageableDefault Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder().search(q).wrapSort(pageable).build();
        var orders = orderService.getAllOrderCombosBySeller(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<SellerOrderComboResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(orders)
                .messages("Orders retrieved successfully")
                .build());
    }

}
