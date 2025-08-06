package org.retrade.main.model.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerProductBaseMetricResponse {
    private Long totalPrice;
    private Long productApprove;
    private Long productActivate;
    private Long productQuantity;
}
