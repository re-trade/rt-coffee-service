package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;
import org.retrade.main.model.constant.TransactionTypeEnum;

import java.math.BigDecimal;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "transactions")
public class TransactionEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = OrderComboEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_combo_id")
    private OrderComboEntity orderCombo;
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;
    @Column(name = "amount")
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TransactionTypeEnum type;
}
