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
public class AdminDashboardMetricResponse {
    private BigDecimal totalUsers;
    private BigDecimal totalOrders;
    private BigDecimal totalProducts;
    private BigDecimal totalCategories;
    private BigDecimal totalSellers;
}
