package org.retrade.main.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.response.OrderStatusResponse;
import org.retrade.main.service.OrderStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("order-status")
public class OrderStatusController {

    private final OrderStatusService orderStatusService;

    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    @GetMapping()
    public ResponseEntity<ResponseObject<List<OrderStatusResponse>>> getAllStatusTrue() {
        List<OrderStatusResponse> result = orderStatusService.getAllStatusTrue();
        return ResponseEntity.ok(new ResponseObject.Builder<List<OrderStatusResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Orders status retrieved successfully")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    @GetMapping("all")
    public ResponseEntity<ResponseObject<List<OrderStatusResponse>>> getAllStatusTrueForSellerChange() {
        List<OrderStatusResponse> result = orderStatusService.getAllStatusTrueForSellerChange();
        return ResponseEntity.ok(new ResponseObject.Builder<List<OrderStatusResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Orders status retrieved successfully")
                .build());
    }
}
