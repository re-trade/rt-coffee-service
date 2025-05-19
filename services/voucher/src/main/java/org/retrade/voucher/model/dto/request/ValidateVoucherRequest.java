package org.retrade.voucher.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateVoucherRequest {
    @NotEmpty(message = "Voucher code is required")
    private String code;
    
    @NotEmpty(message = "Account ID is required")
    private String accountId;
    
    @NotNull(message = "Order total is required")
    @Min(value = 0, message = "Order total must be greater than or equal to 0")
    private BigDecimal orderTotal;
    
    private List<String> productIds; // List of product IDs in the order
}
