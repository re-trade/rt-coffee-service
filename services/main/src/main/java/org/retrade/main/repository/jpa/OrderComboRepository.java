package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderComboRepository extends BaseJpaRepository<OrderComboEntity, String> {
    List<OrderComboEntity> findBySeller(SellerEntity seller);
    List<OrderComboEntity> findByOrderDestination(OrderDestinationEntity orderDestination);
    List<OrderComboEntity> findByOrderStatus(OrderStatusEntity orderStatus);
    List<OrderComboEntity> findBySellerAndOrderStatus(SellerEntity seller, OrderStatusEntity orderStatus);
    List<OrderComboEntity> findByOrderItems_Order_Id(String orderId);

    boolean existsByOrderDestination_Order_CustomerAndId(@NonNull CustomerEntity customer, @NonNull String id);

    Optional<OrderComboEntity> findByIdAndSeller(String id, SellerEntity seller);
    Optional<OrderComboEntity> findByIdAndOrderDestination(String id, OrderDestinationEntity orderDestination);

    @Query("SELECT COALESCE(SUM(o.grandPrice), 0) FROM order_combos o WHERE o.seller = :seller AND o.orderStatus = :status")
    BigDecimal getTotalGrandPriceBySellerAndStatus(@Param("seller") SellerEntity seller, @Param("status") OrderStatusEntity status);

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
}
