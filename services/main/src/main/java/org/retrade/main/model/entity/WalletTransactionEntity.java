package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "wallet_transactions")
public class WalletTransactionEntity extends BaseSQLEntity {
    @Column(name = "amount", nullable = false, updatable = false)
    private BigDecimal amount;
    @Column(name = "note", length = 255, nullable = false, updatable = false)
    private String note;
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;
}
