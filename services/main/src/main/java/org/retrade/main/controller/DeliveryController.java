package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.response.DeliveryResponse;
import org.retrade.main.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;

    @GetMapping("combo/{id}")
    @Operation(summary = "Get delivery info by order combo id")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<DeliveryResponse>> getDeliveryInfoByOrderId(
            @Parameter(description = "Order Combo Id", required = true)
            @PathVariable String id) {
        var response = deliveryService.getDeliveryByOrderComboId(id);
        return ResponseEntity.ok(new ResponseObject.Builder<DeliveryResponse>()
                .success(true)
                .code("SUCCESS")
                .content(response)
                .messages("Order retrieved successfully")
                .build());
    }
}
