package org.retrade.main.model.dto.request;

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
public class AddToCartRequest {
    @NotEmpty(message = "Product ID is required")
    @NotNull(message = "Product ID cannot be null")
    private String productId;
}
