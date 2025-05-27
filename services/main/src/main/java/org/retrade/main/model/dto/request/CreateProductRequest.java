package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    @NotEmpty(message = "Product name is required")
    @NotNull(message = "Product name cannot be null")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;

    @NotEmpty(message = "Short description is required")
    @NotNull(message = "Short description cannot be null")
    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 256, message = "Thumbnail URL must not exceed 256 characters")
    private String thumbnail;

    private Set<String> productImages;

    @NotEmpty(message = "Brand is required")
    @NotNull(message = "Brand cannot be null")
    @Size(max = 128, message = "Brand must not exceed 128 characters")
    private String brand;

    @Size(max = 50, message = "Discount must not exceed 50 characters")
    private String discount;

    @NotEmpty(message = "Model is required")
    @NotNull(message = "Model cannot be null")
    @Size(max = 128, message = "Model must not exceed 128 characters")
    private String model;

    @NotNull(message = "Current price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Current price must be greater than 0")
    private BigDecimal currentPrice;

    private Set<String> categories;
    private Set<String> keywords;
    private Set<String> tags;
}
