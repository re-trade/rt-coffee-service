package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevenueStatResponse {
    private BigDecimal totalRevenue;
    private Long totalOrder;
    private BigDecimal averageOrderValue;
    private Long totalItemsSold;
}
