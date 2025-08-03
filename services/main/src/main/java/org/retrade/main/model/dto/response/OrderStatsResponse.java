package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderStatsResponse {
    private long totalOrders;
    private BigDecimal totalPaymentCost;
    private long totalOrdersCompleted;
    private long totalOrdersBeingDelivered;
}
