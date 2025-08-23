package org.retrade.main.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.response.OrderStatusResponse;
import org.retrade.main.service.OrderStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("order-status")
public class OrderStatusController {

    private final OrderStatusService orderStatusService;

    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    @GetMapping("")
    public ResponseEntity<ResponseObject<List<OrderStatusResponse>>> getAllActiveStatuses() {
        List<OrderStatusResponse> result = orderStatusService.getAllStatusTrue();
        return ResponseEntity.ok(new ResponseObject.Builder<List<OrderStatusResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Trạng thái đơn hàng đang hoạt động đã được lấy thành công")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    @GetMapping("seller-options")
    public ResponseEntity<ResponseObject<List<OrderStatusResponse>>> getAvailableStatusesForSeller() {
        List<OrderStatusResponse> result = orderStatusService.getAllStatusTrueForSellerChange();
        return ResponseEntity.ok(new ResponseObject.Builder<List<OrderStatusResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Các trạng thái khả dụng cho người bán đã được lấy thành công")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    @GetMapping("next-step/{orderComboId}")
    public ResponseEntity<ResponseObject<List<OrderStatusResponse>>> getNextAvailableStatuses(@PathVariable String orderComboId) {
        List<OrderStatusResponse> result = orderStatusService.getAllStatusNextStep(orderComboId);
        return ResponseEntity.ok(new ResponseObject.Builder<List<OrderStatusResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Các trạng thái tiếp theo của đơn hàng đã được lấy thành công")
                .build());
    }
}
