package org.retrade.main.model.entity;


import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "order_combos")
public class OrderComboEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = SellerEntity.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private SellerEntity seller;
    @Column(name = "grand_price", nullable = false)
    private BigDecimal grandPrice;
    @ManyToOne(targetEntity = OrderDestinationEntity.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_destination_id", nullable = false)
    private OrderDestinationEntity orderDestination;
    @ManyToOne(targetEntity = OrderStatusEntity.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_status_id", nullable = false)
    private OrderStatusEntity orderStatus;
}
