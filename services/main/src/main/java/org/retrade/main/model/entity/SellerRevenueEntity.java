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
@Entity(name = "seller_revenues")
public class SellerRevenueEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = OrderComboEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_combo_id", nullable = false)
    private OrderComboEntity orderCombo;
    @Column(name = "total_amount", nullable = false, updatable = false)
    private BigDecimal totalAmount;
    @Column(name = "platform_fee_rate", nullable = false, updatable = false)
    private double platformFeeRate;
    @Column(name = "platform_fee_amount", nullable = false, updatable = false)
    private BigDecimal platformFeeAmount;
    @Column(name = "seller_revenue", nullable = false, updatable = false)
    private BigDecimal sellerRevenue;
}
