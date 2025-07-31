package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.DashboardMetricCodes;
import org.retrade.main.model.constant.OrderStatusCodes;
import org.retrade.main.model.dto.response.*;
import org.retrade.main.model.projection.RevenueMonthProjection;
import org.retrade.main.repository.jpa.OrderComboRepository;
import org.retrade.main.repository.jpa.OrderItemRepository;
import org.retrade.main.repository.jpa.OrderStatusRepository;
import org.retrade.main.repository.jpa.ProductRepository;
import org.retrade.main.service.DashboardService;
import org.retrade.main.util.AuthUtils;
import org.retrade.main.util.DateUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final OrderComboRepository orderComboRepository;
    private final AuthUtils authUtils;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusRepository orderStatusRepository;

    @Override
    public List<DashboardMetricResponse> getSellerDashboardMetric(LocalDateTime fromDate, LocalDateTime toDate) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.getSeller() == null) {
            throw new ValidationException("Seller is not found");
        }
        var seller = account.getSeller();
        var orderStatus = orderStatusRepository.findByCode(OrderStatusCodes.COMPLETED)
                .orElseThrow(() -> new ValidationException("Order status not found"));

        long totalProduct = productRepository.countBySeller(seller);
        BigDecimal currentRevenue = orderComboRepository.getTotalGrandPriceBySellerAndStatusAndDateRange(
                seller, orderStatus, fromDate, toDate);
        long currentOrders = orderComboRepository.countBySellerAndOrderStatusAndCreatedDateBetween(
                seller, orderStatus, Timestamp.valueOf(fromDate), Timestamp.valueOf(toDate));
        double avgVote = productRepository.getAverageVote(seller);
        long cancelOrders = orderComboRepository.countBySellerAndCancelledReasonNotNullAndCreatedDateBetween(
                seller, Timestamp.valueOf(fromDate), Timestamp.valueOf(toDate));
        double returnRate = 0.0;
        if (currentOrders > 0) {
            returnRate = ((double) cancelOrders / currentOrders) * 100;
        }

        LocalDateTime previousFromDate = DateUtils.getPreviousFromDate(fromDate, toDate);
        LocalDateTime previousToDate = DateUtils.getPreviousToDate(fromDate);

        BigDecimal previousRevenue = orderComboRepository.getTotalGrandPriceBySellerAndStatusAndDateRange(
                seller, orderStatus, previousFromDate, previousToDate);
        long previousOrders = orderComboRepository.countBySellerAndOrderStatusAndCreatedDateBetween(
                seller, orderStatus, Timestamp.valueOf(previousFromDate), Timestamp.valueOf(previousToDate));

        double changeRevenue = 0.0;
        if (previousRevenue != null && previousRevenue.compareTo(BigDecimal.ZERO) != 0) {
            changeRevenue = DateUtils.calculatePercentageChange(
                    currentRevenue.doubleValue(), previousRevenue.doubleValue());
        }
        double changeOrders = DateUtils.calculatePercentageChange(
                currentOrders, previousOrders);

        long countActiveProducts = productRepository.countBySellerAndQuantityGreaterThan(seller, 0);
        long totalQuantityInStock = productRepository.sumQuantityBySeller(seller);
        long totalQuantitySold = orderItemRepository.getTotalProductSoldBySellerAndStatusAndDateRange(
                seller, orderStatus, Timestamp.valueOf(fromDate), Timestamp.valueOf(toDate));
        double soldRate = 0.0;
        if (totalQuantityInStock + totalQuantitySold > 0) {
            soldRate = ((double) totalQuantitySold / (totalQuantityInStock + totalQuantitySold)) * 100;
        }

        long totalVerifiedProducts = productRepository.countBySellerAndVerifiedTrue(seller);
        double verifiedRate = 0.0;
        if (totalProduct > 0) {
            verifiedRate = ((double) totalVerifiedProducts / totalProduct) * 100;
        }

        return List.of(
                new DashboardMetricResponse(DashboardMetricCodes.TOTAL_PRODUCTS, totalProduct, 0.0),
                new DashboardMetricResponse(DashboardMetricCodes.REVENUE, currentRevenue, changeRevenue),
                new DashboardMetricResponse(DashboardMetricCodes.TOTAL_ORDERS, currentOrders, changeOrders),
                new DashboardMetricResponse(DashboardMetricCodes.RETURN_RATE, returnRate, 0.0),
                new DashboardMetricResponse(DashboardMetricCodes.AVERAGE_VOTE, avgVote, 0.0),
                new DashboardMetricResponse(DashboardMetricCodes.ACTIVE_PRODUCTS, countActiveProducts, 0.0),
                new DashboardMetricResponse(DashboardMetricCodes.SOLD_RATE, soldRate, 0.0),
                new DashboardMetricResponse(DashboardMetricCodes.VERIFIED_RATE, verifiedRate, 0.0)
        );
    }

    @Override
    public List<RevenuePerMonthResponse> getRevenuePerMonth(int year) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.getSeller() == null) {
            throw new ValidationException("Seller is not found");
        }
        var seller = account.getSeller();
        var rawResult =  orderComboRepository.getRevenuePerMonth(seller, year);
        Map<Integer, BigDecimal> map = rawResult.stream()
                .collect(Collectors.toMap(
                        RevenueMonthProjection::getMonth,
                        RevenueMonthProjection::getTotal
                ));
        return IntStream.rangeClosed(1, 12)
                .mapToObj(month -> RevenuePerMonthResponse.builder()
                        .month(month)
                        .total(map.getOrDefault(month, BigDecimal.ZERO))
                        .build())
                .toList();
    }

    @Override
    public List<OrderStatusCountResponse> getOrderStatusCounts() {
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.getSeller() == null) {
            throw new ValidationException("Seller is not found");
        }
        var seller = account.getSeller();
        var result =  orderComboRepository.getOrderStatusCounts(seller);
        return result.stream().map(item -> OrderStatusCountResponse.builder()
                .code(item.getCode())
                .count(item.getCount())
                .build()).toList();
    }

    @Override
    public List<RecentOrderResponse> getRecentOrders(int limit) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.getSeller() == null) {
            throw new ValidationException("Seller is not found");
        }
        var seller = account.getSeller();
        var result =  orderComboRepository.getRecentOrders(seller, PageRequest.of(0, limit));
        return result.stream().map(item -> RecentOrderResponse.builder()
                .id(item.getId())
                .grandPrice(item.getGrandPrice())
                .createdDate(item.getCreatedDate().toLocalDateTime())
                .receiverName(item.getReceiverName())
                .build()).toList();
    }

    @Override
    public List<TopSellingProductResponse> getBestSellerProducts() {
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.getSeller() == null) {
            throw new ValidationException("Seller is not found");
        }
        var seller = account.getSeller();
        var response = orderItemRepository.getBestSellerProducts(seller);
        return response.stream().map(item -> TopSellingProductResponse.builder()
                .productName(item.getProductName())
                .quantitySold(item.getQuantitySold())
                .revenue(item.getRevenue())
                .build()).toList();
    }

}
