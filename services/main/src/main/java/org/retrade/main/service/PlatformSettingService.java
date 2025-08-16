package org.retrade.main.service;

import java.math.BigDecimal;

public interface PlatformSettingService {
    String getStringValue(String key);

    BigDecimal getDecimalValue(String key);

    boolean getBooleanValue(String key);

    BigDecimal findFeeRate(BigDecimal grandPrice);
}
