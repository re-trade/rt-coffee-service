package org.retrade.voucher.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "voucher_vaults")
public class VoucherVaultEntity extends BaseSQLEntity {
    @Column(name = "account_id", nullable = false)
    private String accountId;
    @ManyToOne(targetEntity = VoucherEntity.class, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private VoucherEntity voucher;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "claimed_date", nullable = false)
    private Timestamp claimedDate;
    @Column(name = "used_date")
    private Timestamp usedDate;
    @Column(name = "expired_date", nullable = false)
    private Timestamp expiredDate;
}
