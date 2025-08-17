package org.retrade.main.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "platform_fee_tiers",
        indexes = {
                @Index(name = "idx_min_price", columnList = "min_price"),
                @Index(name = "idx_max_price", columnList = "max_price")
        }
)
@Entity(name = "platform_fee_tiers")
public class PlatformFeeTierEntity extends BaseSQLEntity {
    @Column(name = "min_price", nullable = false)
    private BigDecimal minPrice;
    @Column(name = "max_price")
    private BigDecimal maxPrice;
    @Column(name = "fee_rate", nullable = false)
    private BigDecimal feeRate;
    @Column(name = "description", nullable = false, length = 255)
    private String description;
}
