package org.retrade.voucher.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "voucher_usages")
public class VoucherUsageEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = VoucherEntity.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "voucher_id", nullable = false)
    private VoucherEntity voucher;
    @ManyToOne(targetEntity = VoucherVaultEntity.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "vault_id", nullable = false)
    private VoucherVaultEntity voucherVault;
    @Column(name = "order_id", nullable = false)
    private String orderId;
    @Column(name = "user_id", nullable = false)
    private String userId;
    @Column(name = "usage_date", nullable = false)
    private LocalDateTime usageDate;
    @Column(name = "discount_applied", nullable = false)
    private BigDecimal discountApplied;
    @Column(name = "type", nullable = false)
    private String type;
    @Column(name = "failure_reason", length = 255)
    private String failureReason;
}
