package org.retrade.voucher.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyVoucherRequest {
    @NotEmpty(message = "Voucher code is required")
    private String code;
    
    @NotEmpty(message = "Account ID is required")
    private String accountId;
    
    @NotEmpty(message = "Order ID is required")
    private String orderId;
    
    private BigDecimal orderTotal;
}
