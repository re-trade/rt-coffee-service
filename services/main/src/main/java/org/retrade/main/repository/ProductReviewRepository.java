package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends BaseJpaRepository<ProductReviewEntity, String> {

    List<ProductReviewEntity> findByProduct(ProductEntity product);

    List<ProductReviewEntity> findBySeller(SellerEntity seller);

    List<ProductReviewEntity> findBySellerAndStatusTrue(SellerEntity seller);

    Page<ProductReviewEntity> findBySeller(SellerEntity seller, Pageable pageable);

    List<ProductReviewEntity> findByCustomer(CustomerEntity customer);

    List<ProductReviewEntity> findByOrderCombo(OrderComboEntity orderCombo);

    List<ProductReviewEntity> findByProductAndVoteGreaterThanEqual(ProductEntity product, Double minVote);

    List<ProductReviewEntity> findByProductAndStatusTrue(ProductEntity product);

//    @Query("SELECT AVG(p.avgVote) FROM products p WHERE p.seller = :seller AND p.avgVote > 0")
//    Double calculateAverageRatingBySeller(@Param("seller") SellerEntity seller);

    @Query("SELECT AVG(p.vote) FROM product_reviews p WHERE p.product = :product AND p.vote > 0")
    Double calculateTotalRatingByProduct(@Param("product") ProductEntity product);

    @Query("""
                SELECT AVG(p.vote)
                FROM product_reviews p
                WHERE p.product = :product AND p.vote > 0 AND p.status = true
            """)
    Double calculateTotalRatingByProductWithStatusTrue(@Param("product") ProductEntity product);

    @Query("SELECT DISTINCT r.product FROM product_reviews r WHERE r.createdDate > :since OR r.updatedDate > :since")
    List<ProductEntity> findProductsWithRecentReviews(@Param("since") LocalDateTime since);

    @Query("SELECT AVG(r.vote) FROM product_reviews r WHERE r.product = :product AND r.status = true")
    Double findAverageRatingByProduct(@Param("product") ProductEntity product);

}
