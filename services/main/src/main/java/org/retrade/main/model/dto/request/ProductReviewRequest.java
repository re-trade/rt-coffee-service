package org.retrade.main.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.entity.CustomerEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewRequest {
    String orderId;
    String productId;
    private CustomerEntity customer;
    private Double vote;
    private String content;
}
