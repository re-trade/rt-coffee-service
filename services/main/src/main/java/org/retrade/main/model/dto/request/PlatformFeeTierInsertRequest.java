package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformFeeTierInsertRequest {
    @NotNull(message = "Min price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Min price must be >= 0")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Max price must be > 0")
    private BigDecimal maxPrice;

    @NotNull(message = "Fee rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Fee rate must be >= 0")
    @DecimalMax(value = "1.0", inclusive = true, message = "Fee rate must be <= 1")
    private BigDecimal feeRate;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must be less than or equal to 255 characters")
    private String description;
}
