package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateProductReviewRequest {
    @NotBlank(message = "Order ID is required")
    private String orderId;
    @NotBlank(message = "Product ID is required")
    private String productId;
    @NotBlank(message = "Content is required")
    private String content;
    @DecimalMin(value = "0.0", message = "Vote must be between 0 and 5")
    @DecimalMax(value = "5.0", message = "Vote must be between 0 and 5")
    private double vote;
    private Set<String> imageReview;
}
