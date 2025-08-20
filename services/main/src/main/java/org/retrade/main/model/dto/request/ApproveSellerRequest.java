package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveSellerRequest {
    @NotBlank(message = "Seller ID must not be blank")
    private String sellerId;
    private Boolean forced;
    @NotNull(message = "Approve flag must not be null")
    private Boolean approve;
    private String reason;
}
