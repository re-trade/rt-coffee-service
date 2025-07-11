package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReportSellerRequest {
    @NotBlank(message = "Seller ID is required")
    private String sellerId;

    @NotBlank(message = "Report type is required")
    private String typeReport;

    @NotBlank(message = "Content is required")
    private String content;

    private String orderId;

    private String productId;

    private String image;
}
