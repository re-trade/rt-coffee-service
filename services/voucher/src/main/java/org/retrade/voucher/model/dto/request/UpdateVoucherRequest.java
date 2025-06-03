package org.retrade.voucher.model.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.voucher.model.constant.VoucherTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVoucherRequest {
    private String code;
    private VoucherTypeEnum type;
    private Double discount;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private Boolean active;
    
    @Min(value = 0, message = "Max uses must be greater than or equal to 0")
    private Integer maxUses;
    
    @Min(value = 0, message = "Max uses per user must be greater than or equal to 0")
    private Integer maxUsesPerUser;
    
    @Min(value = 0, message = "Minimum spend must be greater than or equal to 0")
    private BigDecimal minSpend;
    
    private List<String> productRestrictions;
}
