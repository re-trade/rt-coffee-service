package org.retrade.main.service;

import org.retrade.main.model.dto.response.DashboardMetricResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface DashboardService {
    List<DashboardMetricResponse> getSellerDashboardMetric(LocalDateTime fromDate, LocalDateTime toDate);
}
