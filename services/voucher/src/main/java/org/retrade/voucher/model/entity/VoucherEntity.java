package org.retrade.voucher.model.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "vouchers")
public class VoucherEntity extends BaseSQLEntity {
    private String code;
    private String type;
    private Double discount;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private Boolean actived;
    private Integer maxUses;
    private Integer maxUsesPerUser;
    private Integer currentUses;
    private Integer minSpend;
}
