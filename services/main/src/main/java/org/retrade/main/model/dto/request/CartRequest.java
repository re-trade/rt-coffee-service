package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartRequest {
    @NotEmpty(message = "Product ID is required")
    @NotNull(message = "Product ID cannot be null")
    private String productId;
    @NotNull(message = "Product ID cannot be null")
    @Min(message = "Quantity must be at least 1", value = 1)
    private Integer quantity;
}
