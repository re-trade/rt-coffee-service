package org.retrade.main.model.projection;

import java.math.BigDecimal;

public interface BestSellerProductProjection {
    String getProductName();
    Long getQuantitySold();
    BigDecimal getRevenue();
}
