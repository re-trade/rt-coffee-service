package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

}
