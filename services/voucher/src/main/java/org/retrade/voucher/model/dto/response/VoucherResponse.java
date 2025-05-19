package org.retrade.voucher.model.dto.response;

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
public class VoucherResponse {
    private String id;
    private String code;
    private VoucherTypeEnum type;
    private Double discount;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private Boolean active;
    private Integer maxUses;
    private Integer maxUsesPerUser;
    private Integer currentUses;
    private Integer minSpend;
    private List<String> productRestrictions;
}
