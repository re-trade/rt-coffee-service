package org.retrade.main.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "Product IDs are required")
    @NotEmpty(message = "Product IDs cannot be empty")
    @Size(min = 1, max = 100, message = "Product IDs must contain between 1 and 100 items")
    private List<String> productIds;

    @Size(max = 50, message = "Voucher code must not exceed 50 characters")
    private String voucherCode;

    @NotNull(message = "Delivery address is required")
    @Valid
    private OrderAddressRequest deliveryAddress;

    @NotNull(message = "Payment method is required")
    @NotEmpty(message = "Payment method cannot be empty")
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
