package org.retrade.main.model.projection;

import java.math.BigDecimal;
import java.sql.Timestamp;

public interface RecentOrderProjection {
    String getId();
    BigDecimal getGrandPrice();
    Timestamp getCreatedDate();
    String getReceiverName();
}
