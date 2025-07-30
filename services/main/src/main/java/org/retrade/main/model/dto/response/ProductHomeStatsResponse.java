package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductHomeStatsResponse {
    private long totalProducts;
    private long totalSoldProducts;
    private long totaOrders;
    private long totalUsers;
}
