package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRetradeResponse {
    private String id;
    private String productId;
    private Long quantity;
    private Long retradeQuantity;
    private String sellerId;
    private String sellerName;
    private String sellerAvatarUrl;
}
