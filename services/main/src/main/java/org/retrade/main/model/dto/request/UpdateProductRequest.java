package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.constant.ProductConditionEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 256, message = "Thumbnail URL must not exceed 256 characters")
    private String thumbnail;

    private Set<@Size(max = 256, message = "Image URL must not exceed 256 characters") String> productImages;

    @NotEmpty(message = "Brand is required")
    @NotNull(message = "Brand cannot be null")
    private String brandId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private LocalDate warrantyExpiryDate;

    @NotNull(message = "Condition is required")
    private ProductConditionEnum condition;

    @Size(max = 128, message = "Model must not exceed 128 characters")
    private String model;

    @DecimalMin(value = "0.0", inclusive = false, message = "Current price must be greater than 0")
    private BigDecimal currentPrice;

    private Set<@NotBlank(message = "Category ID must not be blank") String> categoryIds;
    private Set<@NotBlank(message = "Tag must not be blank") String> tags;
}
