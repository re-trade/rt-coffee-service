package org.retrade.main.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.response.DashboardMetricResponse;
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
}
