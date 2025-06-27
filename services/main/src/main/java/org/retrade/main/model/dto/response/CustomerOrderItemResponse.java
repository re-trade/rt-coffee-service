package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderItemResponse {
    private String itemId;
    private String itemName;
    private String itemThumbnail;
    private String productId;
    private BigDecimal basePrice;
    private Double discount;
    private Integer quantity;
}
