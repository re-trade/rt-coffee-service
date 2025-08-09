package org.retrade.main.model.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrderBaseMetricResponse {
    private Long totalOrder;
    private Long orderCompleted;
    private Long orderCancelled;
    private BigDecimal totalPaymentReceived;
}
