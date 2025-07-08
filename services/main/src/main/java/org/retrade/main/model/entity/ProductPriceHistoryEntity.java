package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "product_price_histories")
public class ProductPriceHistoryEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = ProductEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
    @Column(name = "old_price", nullable = false)
    private BigDecimal oldPrice;
    @Column(name = "new_price", nullable = false)
    private BigDecimal newPrice;
    @Column(name = "update_date", nullable = false)
    private LocalDateTime fromDate;

}
