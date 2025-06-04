package org.retrade.main.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemEntity implements Serializable {
    private String productId;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}
