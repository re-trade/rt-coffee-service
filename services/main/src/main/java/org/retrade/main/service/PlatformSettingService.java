package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.response.PlatformFeeTierResponse;

import java.math.BigDecimal;
import java.util.List;

public interface PlatformSettingService {
    String getStringValue(String key);

    BigDecimal getDecimalValue(String key);

    boolean getBooleanValue(String key);

    BigDecimal findFeeRate(BigDecimal grandPrice);

    PaginationWrapper<List<PlatformFeeTierResponse>> getAllPlatformFeeTierConfig(QueryWrapper queryWrapper);
}
