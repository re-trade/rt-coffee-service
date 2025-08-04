package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.*;
import org.retrade.main.model.projection.BestSellerProductProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface OrderItemRepository extends BaseJpaRepository<OrderItemEntity, String> {
    List<OrderItemEntity> findByOrder(OrderEntity order);
    List<OrderItemEntity> findByOrderCombo(OrderComboEntity orderCombo);
    List<OrderItemEntity> findByProduct(ProductEntity product);

    boolean existsByProduct_IdAndOrder_Id(@NonNull String id, @NonNull String id1);

    boolean existsByProduct_IdAndOrderCombo_Id(@NonNull String id, @NonNull String id1);

    List<OrderItemEntity> findByOrder_Id(@NonNull String id);

    @Query("""
        SELECT COALESCE(SUM(oi.quantity), 0)
        FROM order_items oi
        WHERE oi.orderCombo.seller = :seller
          AND oi.orderCombo.orderStatus = :status
          AND oi.createdDate BETWEEN :fromDate AND :toDate
    """)
    Long getTotalProductSoldBySellerAndStatusAndDateRange(SellerEntity seller,
                                                          OrderStatusEntity status,
                                                          Timestamp fromDate,
                                                          Timestamp toDate);

    @Query("""
        SELECT COALESCE(SUM(oi.quantity)) FROM order_items oi
        WHERE oi.order.customer = :customer
    """)
    Long sumQuantityByCustomerId(@Param("customer") CustomerEntity customer);

    @Query("""
        SELECT oi.product.name AS productName,
               SUM(oi.quantity) AS quantitySold,
               SUM(oi.basePrice * oi.quantity) AS revenue
        FROM order_items oi
        WHERE oi.product.seller = :seller
        GROUP BY oi.product.name
        ORDER BY quantitySold DESC
    """)
    List<BestSellerProductProjection> getBestSellerProducts(@Param("seller") SellerEntity seller);

}
