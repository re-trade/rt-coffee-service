package org.retrade.main.model.projection;

import java.math.BigDecimal;

public interface RevenueMonthProjection {
    Integer getMonth();
    BigDecimal getTotal();
}
