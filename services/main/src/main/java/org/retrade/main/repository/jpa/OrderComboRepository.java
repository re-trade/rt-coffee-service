package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.*;
import org.retrade.main.model.projection.OrderStatusCountProjection;
import org.retrade.main.model.projection.RecentOrderProjection;
import org.retrade.main.model.projection.RevenueMonthProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderComboRepository extends BaseJpaRepository<OrderComboEntity, String> {
    List<OrderComboEntity> findBySeller(SellerEntity seller);
    List<OrderComboEntity> findByOrderDestination(OrderDestinationEntity orderDestination);
    List<OrderComboEntity> findByOrderItems_Order_Id(String orderId);

    boolean existsByOrderDestination_Order_CustomerAndId(@NonNull CustomerEntity customer, @NonNull String id);

    Optional<OrderComboEntity> findByIdAndSeller(String id, SellerEntity seller);
    Optional<OrderComboEntity> findByIdAndOrderDestination(String id, OrderDestinationEntity orderDestination);

    @Query("SELECT COALESCE(SUM(o.grandPrice), 0) FROM order_combos o WHERE o.seller = :seller AND o.orderStatus = :status")
    BigDecimal getTotalGrandPriceBySellerAndStatus(@Param("seller") SellerEntity seller, @Param("status") OrderStatusEntity status);

    @Query("SELECT COALESCE(SUM(o.grandPrice), 0) FROM order_combos o WHERE o.seller = :seller AND o.orderStatus = :status")
    BigDecimal getTotalGrandPriceBySellerAndStatusAndDateRange(
            @Param("seller") SellerEntity seller,
            @Param("status") OrderStatusEntity status,
            @Param("fromDate")LocalDateTime fromDate,
            @Param("toDate")LocalDateTime toDate
    );

    @Query("SELECT COUNT(o) FROM order_combos o WHERE o.seller = :seller AND o.orderStatus = :status")
    Long countOrdersBySellerAndStatus(@Param("seller") SellerEntity seller, @Param("status") OrderStatusEntity status);



    @Query("SELECT COALESCE(SUM(o.grandPrice * (1 - CASE " +
            "WHEN o.grandPrice < 500000 THEN 0.05 " +
            "WHEN o.grandPrice <= 1000000 THEN 0.04 " +
            "ELSE 0.03 END)), 0) " +
            "FROM order_combos o WHERE o.seller = :seller AND o.orderStatus = :status")
    BigDecimal getTotalPriceAfterFeeBySellerAndStatus(@Param("seller") SellerEntity seller, @Param("status") OrderStatusEntity status);

    @Query("SELECT COALESCE(AVG(o.grandPrice), 0) FROM order_combos o WHERE o.seller = :seller AND o.orderStatus = :status")
    BigDecimal getAverageGrandPriceBySellerAndStatus(@Param("seller") SellerEntity seller, @Param("status") OrderStatusEntity status);

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) " +
            "FROM order_combos oc JOIN oc.orderItems oi " +
            "WHERE oc.seller = :seller AND oc.orderStatus = :status")
    Long getTotalItemsSoldBySellerAndStatus(@Param("seller") SellerEntity seller, @Param("status") OrderStatusEntity status);

    long countDistinctBySeller_IdAndOrderStatus_Code(@NonNull String id, @NonNull String code);

    @Query("SELECT COUNT(o) FROM order_combos o WHERE o.orderStatus.code = 'COMPLETED'")
    long countByOrderStatus();

    long countBySellerAndCancelledReasonNotNullAndCreatedDateBetween(SellerEntity seller, Timestamp createdDateStart, Timestamp createdDateEnd);

    long countBySellerAndOrderStatusAndCreatedDateBetween(@NonNull SellerEntity seller, @NonNull OrderStatusEntity orderStatus, @NonNull Timestamp createdDateStart, @NonNull Timestamp createdDateEnd);

    @Query("""
        SELECT MONTH(o.createdDate) AS month, SUM(o.grandPrice) AS total
        FROM order_combos o
        WHERE o.seller = :seller AND YEAR(o.createdDate) = :year AND o.orderStatus.code = 'COMPLETED'
        GROUP BY MONTH(o.createdDate)
        ORDER BY MONTH(o.createdDate)
    """)
    List<RevenueMonthProjection> getRevenuePerMonth(@Param("seller") SellerEntity seller, @Param("year") int year);

    @Query("""
        SELECT os.code AS code, COUNT(o) AS count
        FROM order_statuses os
        LEFT JOIN order_combos o
          ON o.orderStatus = os AND o.seller = :seller
        GROUP BY os.code
    """)
    List<OrderStatusCountProjection> getOrderStatusCounts(@Param("seller") SellerEntity seller);

    @Query("""
        SELECT o.id AS id, o.grandPrice AS grandPrice, o.createdDate AS createdDate, od.customerName AS receiverName
        FROM order_combos o
        JOIN o.orderDestination od
        WHERE o.seller = :seller
        ORDER BY o.createdDate DESC
    """)
    List<RecentOrderProjection> getRecentOrders(@Param("seller") SellerEntity seller, Pageable pageable);

    long countBySellerAndOrderStatus_Code(SellerEntity seller, String code);

    long countBySeller(@NonNull SellerEntity seller);



    @Query("""
        SELECT COUNT(oc) FROM order_combos oc
        WHERE oc.orderDestination.order.customer = :customer
        AND oc.orderStatus.code = 'COMPLETED'
    """)
    Long countCompletedOrdersByCustomer(@Param("customer") CustomerEntity customer);

    long countBySellerAndOrderStatus_CodeNot(SellerEntity seller, String code);
}
