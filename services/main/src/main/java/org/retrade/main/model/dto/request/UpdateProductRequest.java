package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @Size(max = 128, message = "Brand must not exceed 128 characters")
    private String brand;

    @Size(max = 50, message = "Discount must not exceed 50 characters")
    private Double discount;

    @Size(max = 128, message = "Model must not exceed 128 characters")
    private String model;

    @DecimalMin(value = "0.0", inclusive = false, message = "Current price must be greater than 0")
    private BigDecimal currentPrice;

    private Set<@NotBlank(message = "Category ID must not be blank") String> categoryIds;
    private Set<@NotBlank(message = "Keyword must not be blank") String> keywords;
    private Set<@NotBlank(message = "Tag must not be blank") String> tags;

    private Boolean verified;
}
