package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "order_items")
public class OrderItemEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = OrderEntity.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
    @Column(name = "product_id", nullable = false)
    private String productId;
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    @Column(name = "unit", nullable = false)
    private String unit;
}
