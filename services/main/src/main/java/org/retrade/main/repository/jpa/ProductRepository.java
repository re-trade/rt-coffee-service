package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.OrderEntity;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProductRepository extends BaseJpaRepository<ProductEntity, String> {

    @Query("SELECT p FROM products p JOIN p.categories c WHERE c.name = :categoryName")
    List<ProductEntity> findByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT p FROM products p JOIN p.categories c WHERE c.name = :categoryName")
    Page<ProductEntity> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

    @Query("SELECT oi.product FROM order_items oi WHERE oi.order = :order")
    List<ProductEntity> findProductsByOrder(@Param("order") OrderEntity order);


    @Query(
            value = """
        WITH RECURSIVE product_chain AS (
            SELECT p.id, p.product_parent_id
            FROM main.products p
            WHERE p.id = :productId
          UNION ALL
            SELECT p2.id, p2.product_parent_id
            FROM main.products p2
            JOIN product_chain pc ON p2.id = pc.product_parent_id
        )
        SELECT id FROM product_chain;
    """,
            nativeQuery = true
    )
    Set<String> findProductAncestryIds(@Param("productId") String productId);

    @Query("SELECT AVG(p.avgVote) FROM products p WHERE p.seller = :seller AND p.avgVote > 0")
    Double findAverageRatingBySeller(@Param("seller") SellerEntity seller);
    // Lấy danh sách seller có sản phẩm liên quan
    @Query("SELECT DISTINCT p.seller FROM products p WHERE p IN :products")
    List<SellerEntity> findSellersByProducts(@Param("products") List<ProductEntity> products);


//    @Query("SELECT p FROM products p " +
//            "WHERE p.id IN (SELECT oi.id FROM order_items oi " +
//            "JOIN oi.orderCombo oc " +
//            "WHERE oc.orderStatus.code = 'CONFIRM')")
//    Page<ProductEntity> findBestSellingProducts(Pageable pageable);

    @Query(value = """
    SELECT p.* FROM products p
    JOIN order_items oi ON oi.product_id = p.id
    JOIN order_combos oc ON oi.order_combo_id = oc.id
    JOIN order_statuses os ON oc.order_status_id = os.id
    WHERE os.code = 'CONFIRM'
    GROUP BY p.id
    ORDER BY SUM(oi.quantity) DESC
    """,
            countQuery = """
    SELECT COUNT(DISTINCT p.id) FROM products p
    JOIN order_items oi ON oi.product_id = p.id
    JOIN order_combos oc ON oi.order_combo_id = oc.id
    JOIN order_statuses os ON oc.order_status_id = os.id
    WHERE os.code = 'CONFIRM'
    """,
            nativeQuery = true)
    Page<ProductEntity> findBestSellingProducts(Pageable pageable);



}
