package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends BaseJpaRepository<ProductReviewEntity, String> {
    List<ProductReviewEntity> findByProduct(ProductEntity product);

    List<ProductReviewEntity> findByCustomer(CustomerEntity customer);

    List<ProductReviewEntity> findByOrder(OrderEntity order);

    Optional<ProductReviewEntity> findByProductAndCustomerAndOrder(ProductEntity product, CustomerEntity customer, OrderEntity order);

    List<ProductReviewEntity> findByProductAndVoteGreaterThanEqual(ProductEntity product, Double minVote);

    List<ProductReviewEntity> findByProductAndStatusTrue(ProductEntity product);

    @Query("""
                SELECT r
                FROM product_reviews r
                JOIN FETCH r.product p
                WHERE p.seller = :sellerId AND r.status = true
            """)
    List<ProductReviewEntity> findAllBySellerAndStatusTrueWithProduct(@Param("seller") SellerEntity seller);
    @Query("""
                SELECT r
                FROM product_reviews r
                JOIN FETCH r.product p
                WHERE p.seller = :sellerId
            """)
    List<ProductReviewEntity> findAllBySellerWithProduct(@Param("seller") SellerEntity seller);

    @Query("""
    SELECT AVG(r.vote)
    FROM product_reviews r
    JOIN r.product p
    WHERE p.seller = :seller AND r.status = true
""")
    Double calculateAverageRatingBySeller(@Param("seller") SellerEntity seller);

    @Query("""
    SELECT AVG(r.vote)
    FROM product_reviews r
    WHERE r.product = :product AND r.status = true
""")
    Double calculateTotalRatingByProduct(@Param("product") ProductEntity product);




        @Query("SELECT AVG(r.vote) FROM product_reviews r WHERE r.product = :product AND r.status = true")
        Double findAverageRatingByProduct(@Param("product") ProductEntity product);

        @Query("SELECT AVG(p.avgVote) FROM products p WHERE p.seller = :seller AND p.productRating IS NOT NULL")
        Double findAverageRatingBySeller(@Param("seller") SellerEntity seller);

        // Lấy danh sách sản phẩm có review mới kể từ thời điểm nhất định
        @Query("SELECT DISTINCT r.product FROM product_reviews r WHERE r.createdDate > :since OR r.updatedDate > :since")
        List<ProductEntity> findProductsWithRecentReviews(@Param("since") LocalDateTime since);

        // Lấy danh sách seller có sản phẩm liên quan
        @Query("SELECT DISTINCT p.seller FROM products p WHERE p IN :products")
        List<SellerEntity> findSellersByProducts(@Param("products") List<ProductEntity> products);

}
