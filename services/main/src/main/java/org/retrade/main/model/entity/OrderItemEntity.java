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
@Entity(name = "order_items")
public class OrderItemEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = OrderEntity.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
    @ManyToOne(targetEntity = ProductEntity.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
    @ManyToOne(targetEntity = OrderComboEntity.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_combo_id", nullable = false)
    private OrderComboEntity orderCombo;
    @Column(name = "short_description", nullable = false, columnDefinition = "TEXT")
    private String shortDescription;
    @Column(name = "product_name", nullable = false, length = 128)
    private String productName;
    @Column(name = "background_url", nullable = false, length = 256)
    private String backgroundUrl;
    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;
    @Column(name = "discount", nullable = false, columnDefinition = "NUMERIC(5, 2) DEFAULT 0.00 NOT NULL")
    private Double discount;
    @Column(name = "unit", nullable = false, length = 10)
    private String unit;
}
