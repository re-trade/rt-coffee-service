package org.retrade.voucher.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimVoucherRequest {
    @NotEmpty(message = "Voucher code is required")
    private String code;
    
    @NotEmpty(message = "Account ID is required")
    private String accountId;
}
