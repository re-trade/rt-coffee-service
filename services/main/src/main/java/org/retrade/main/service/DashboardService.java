package org.retrade.main.service;

import org.retrade.main.model.dto.response.*;

import java.time.LocalDateTime;
import java.util.List;

public interface DashboardService {
    List<DashboardMetricResponse> getSellerDashboardMetric(LocalDateTime fromDate, LocalDateTime toDate);

    List<RevenuePerMonthResponse> getRevenuePerMonth(int year);

    List<OrderStatusCountResponse> getOrderStatusCounts();

    List<RecentOrderResponse> getRecentOrders(int limit);

    List<TopSellingProductResponse> getBestSellerProducts();

    SellerProductBaseMetricResponse getSellerProductMetric();

    SellerOrderBaseMetricResponse getSellerOrderMetric();

    AdminDashboardMetricResponse getAdminDashboardMetric();

    List<RevenuePerMonthResponse> getPlatformRevenuePerMonth(int year);

    List<ReviewMetricResponse> getProductReviewStatusMetricResponse();
}
