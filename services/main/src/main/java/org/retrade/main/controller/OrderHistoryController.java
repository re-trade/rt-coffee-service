package org.retrade.main.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.CreateOrderHistoryRequest;
import org.retrade.main.model.dto.response.OrderHistoryResponse;
import org.retrade.main.model.dto.response.OrderStatusResponse;
import org.retrade.main.service.OrderHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("order-history")
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;

    @GetMapping("order-combo/{id}")
    public ResponseEntity<ResponseObject<List<OrderHistoryResponse>>> getAllNotesByOrderComboId(@PathVariable String id) {
        List<OrderHistoryResponse> result = orderHistoryService.getAllNotesByOrderComboId(id);
        return ResponseEntity.ok(new ResponseObject.Builder<List<OrderHistoryResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Trạng thái đơn hàng đã được lấy thành công")
                .build());
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseObject<OrderHistoryResponse>> getDetailsOrderHistory(@PathVariable String id) {
        OrderHistoryResponse result = orderHistoryService.getDetailsOrderHistory(id);
        return ResponseEntity.ok(new ResponseObject.Builder<OrderHistoryResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Lịch sử đơn hàng đã được lấy thành công")
                .build());
    }

    @PostMapping
    public ResponseEntity<ResponseObject<OrderHistoryResponse>> createOrderHistory(@RequestBody CreateOrderHistoryRequest request) {
        OrderHistoryResponse result = orderHistoryService.createOrderHistory(request);
        return ResponseEntity.ok(new ResponseObject.Builder<OrderHistoryResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Tạo lịch sử đơn hàng thành công")
                .build());
    }
    @PutMapping("{id}")
    public ResponseEntity<ResponseObject<OrderHistoryResponse>> updateOrderHistory(@PathVariable String id, @RequestParam String notes) {
        OrderHistoryResponse result = orderHistoryService.updateOrderHistory(id);
        return ResponseEntity.ok(new ResponseObject.Builder<OrderHistoryResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Cập nhật lịch sử đơn hàng thành công")
                .build());
    }
}
