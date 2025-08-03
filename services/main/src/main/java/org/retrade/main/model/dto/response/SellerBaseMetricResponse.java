package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerBaseMetricResponse {
    private Long productQuantity;
    private Double avgVote;
    private Long totalOrder;
    private Long totalOrderSold;
}
