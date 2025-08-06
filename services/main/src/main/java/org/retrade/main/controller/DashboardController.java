package org.retrade.main.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.response.*;
import org.retrade.main.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("seller/metric")
    public ResponseEntity<ResponseObject<List<DashboardMetricResponse>>> getSellerDashboardMetric(
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate
    ) {
        var result = dashboardService.getSellerDashboardMetric(fromDate, toDate);
        return ResponseEntity.ok(new ResponseObject.Builder<List<DashboardMetricResponse>>()
                .success(true)
                .code("SELLER_DASHBOARD_RETRIEVED")
                .content(result)
                .messages("Seller Dashboard retrieved successfully")
                .build());
    }

    @GetMapping("seller/revenue")
    public ResponseEntity<ResponseObject<List<RevenuePerMonthResponse>>> getRevenuePerMonth(int year) {
        var result = dashboardService.getRevenuePerMonth(year);
        return ResponseEntity.ok(new ResponseObject.Builder<List<RevenuePerMonthResponse>>()
                .success(true)
                .code("SELLER_DASHBOARD_RETRIEVED")
                .content(result)
                .messages("Seller Dashboard retrieved successfully")
                .build());
    }

    @GetMapping("seller/order-count")
    public ResponseEntity<ResponseObject<List<OrderStatusCountResponse>>> getOrderStatusCount() {
        var result = dashboardService.getOrderStatusCounts();
        return ResponseEntity.ok(new ResponseObject.Builder<List<OrderStatusCountResponse>>()
                .success(true)
                .code("SELLER_DASHBOARD_RETRIEVED")
                .content(result)
                .messages("Seller Dashboard retrieved successfully")
                .build());
    }


    @GetMapping("seller/order")
    public ResponseEntity<ResponseObject<List<RecentOrderResponse>>> getRecentOrders(int limit) {
        var result = dashboardService.getRecentOrders(limit);
        return ResponseEntity.ok(new ResponseObject.Builder<List<RecentOrderResponse>>()
                .success(true)
                .code("SELLER_DASHBOARD_RETRIEVED")
                .content(result)
                .messages("Seller Dashboard retrieved successfully")
                .build());
    }

    @GetMapping("seller/best-product")
    public ResponseEntity<ResponseObject<List<TopSellingProductResponse>>> getBestSellerProducts() {
        var result = dashboardService.getBestSellerProducts();
        return ResponseEntity.ok(new ResponseObject.Builder<List<TopSellingProductResponse>>()
                .success(true)
                .code("SELLER_DASHBOARD_RETRIEVED")
                .content(result)
                .messages("Seller Dashboard retrieved successfully")
                .build());
    }

    @GetMapping("seller/product/metric")
    public ResponseEntity<ResponseObject<SellerProductBaseMetricResponse>> getSellerProductMetric() {
        var result = dashboardService.getSellerProductMetric();
        return ResponseEntity.ok(new ResponseObject.Builder<SellerProductBaseMetricResponse>()
                .success(true)
                .code("SELLER_DASHBOARD_RETRIEVED")
                .content(result)
                .messages("Seller Dashboard retrieved successfully")
                .build());
    }

}
