package org.retrade.main.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartEntity implements Serializable {
    private String customerId;
    @Builder.Default
    private Map<String, Set<CartItemEntity>> shopItems = new HashMap<>();
    private LocalDateTime lastUpdated;
}
