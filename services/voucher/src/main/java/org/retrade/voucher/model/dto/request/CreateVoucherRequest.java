package org.retrade.voucher.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.voucher.model.constant.VoucherTypeEnum;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVoucherRequest {
    @NotEmpty(message = "Code is required")
    private String code;
    
    @NotNull(message = "Type is required")
    private VoucherTypeEnum type;
    
    @NotNull(message = "Discount is required")
    @Min(value = 0, message = "Discount must be greater than or equal to 0")
    private Double discount;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    @NotNull(message = "Expiry date is required")
    private LocalDateTime expiryDate;
    
    @NotNull(message = "Active status is required")
    private Boolean active;
    
    @Min(value = 0, message = "Max uses must be greater than or equal to 0")
    private Integer maxUses;
    
    @Min(value = 0, message = "Max uses per user must be greater than or equal to 0")
    private Integer maxUsesPerUser;
    
    @Min(value = 0, message = "Minimum spend must be greater than or equal to 0")
    private Integer minSpend;
    
    private List<String> productRestrictions;
}
