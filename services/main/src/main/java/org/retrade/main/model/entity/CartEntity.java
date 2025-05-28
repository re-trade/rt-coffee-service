package org.retrade.main.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartEntity implements Serializable {
    private String userId;
    
    @Builder.Default
    private List<CartItemEntity> items = new ArrayList<>();
    
    private Integer totalItems;
    private BigDecimal totalAmount;
    private LocalDateTime lastUpdated;
}
