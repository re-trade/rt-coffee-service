package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.response.RevenueResponse;
import org.retrade.main.model.dto.response.RevenueStatResponse;

import java.util.List;

public interface RevenueService {
    PaginationWrapper <List<RevenueResponse>> getMyRevenue(QueryWrapper queryWrapper);

    RevenueStatResponse getStatsRevenue();
}
