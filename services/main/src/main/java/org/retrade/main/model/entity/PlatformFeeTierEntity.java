package org.retrade.main.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
