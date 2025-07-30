package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.OrderStatusCodes;
import org.retrade.main.model.dto.response.DashboardMetricResponse;
import org.retrade.main.repository.jpa.OrderComboRepository;
import org.retrade.main.repository.jpa.OrderItemRepository;
import org.retrade.main.repository.jpa.OrderStatusRepository;
import org.retrade.main.repository.jpa.ProductRepository;
import org.retrade.main.service.DashboardService;
import org.retrade.main.util.AuthUtils;
import org.retrade.main.util.DateUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

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
        BigDecimal currentRevenue = orderComboRepository.getTotalGrandPriceBySellerAndStatusAndDateRange(seller, orderStatus, fromDate, toDate);
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
                seller, orderStatus, Timestamp.valueOf(fromDate), Timestamp.valueOf(toDate)
        );
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
                new DashboardMetricResponse("Tổng sản phẩm", String.valueOf(totalProduct), "", 0.0, "toàn bộ"),
                new DashboardMetricResponse("Doanh thu", currentRevenue.toString(), "đ", changeRevenue, "so với kỳ trước"),
                new DashboardMetricResponse("Đơn hàng", String.valueOf(currentOrders), "", changeOrders, "so với kỳ trước"),
                new DashboardMetricResponse("Tỷ lệ hoàn hàng", String.format("%.2f", returnRate), "%", 0.0, "so với kỳ trước"),
                new DashboardMetricResponse("Đánh giá trung bình", String.format("%.1f/5.0", avgVote), "", 0.0, "toàn bộ"),
                new DashboardMetricResponse("Sản phẩm còn bán được", String.valueOf(countActiveProducts), "", 0.0, "toàn bộ"),
                new DashboardMetricResponse("Tỷ lệ sản phẩm đã bán", String.format("%.2f", soldRate), "%", 0.0, "toàn bộ"),
                new DashboardMetricResponse("Tỷ lệ sản phẩm đã duyệt", String.format("%.2f", verifiedRate), "%", 0.0, "toàn bộ")
        );

    }
}
