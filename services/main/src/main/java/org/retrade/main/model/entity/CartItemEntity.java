package org.retrade.main.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemEntity implements Serializable {
    private String productId;
    private Integer quantity;
    private BigDecimal priceSnapshot;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}
