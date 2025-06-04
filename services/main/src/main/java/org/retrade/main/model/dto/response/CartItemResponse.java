package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private String productId;
    private String productName;
    private String productThumbnail;
    private String productBrand;
    private BigDecimal totalPrice;
    private LocalDateTime addedAt;
    private Boolean productAvailable;
}
