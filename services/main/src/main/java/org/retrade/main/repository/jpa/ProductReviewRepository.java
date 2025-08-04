package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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

    @Query("""
    SELECT r
    FROM product_reviews r
    WHERE r.status = true
      AND r.vote = :vote
      AND r.seller = :seller
""")
    Page<ProductReviewEntity> findReviewsBySellerAndStatusIsTrueWithVote(
            @Param("vote") double vote,
            @Param("seller") SellerEntity seller,
            Pageable pageable
    );

//    @Query("""
//        SELECT r
//        FROM product_reviews r
//        WHERE r.status = true
//          AND r.seller = :seller
//          AND (:vote IS NULL OR r.vote = :vote)
//          AND (:keyword IS NULL OR LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
//    """)
//    Page<ProductReviewEntity> findProductReviewsBySellerAndKeyword(
//            @Param("vote") Double vote,
//            @Param("seller") SellerEntity seller,
//            @Param("keyword") String keyword,
//            Pageable pageable
//    );
@Query("""
    SELECT r
    FROM product_reviews r
    JOIN r.customer c
    JOIN r.product p
    WHERE r.status = true
      AND r.seller = :seller
      AND (:vote IS NULL OR r.vote = :vote)
      AND (
        :keyword IS NULL OR (
          LOWER(CAST(r.content AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(CAST(r.replyContent AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
      )
""")
Page<ProductReviewEntity> findProductReviewsBySellerAndKeyword(
        @Param("vote") Double vote,
        @Param("seller") SellerEntity seller,
        @Param("keyword") String keyword,
        Pageable pageable
);



    @Query("""
    SELECT COUNT(r)
    FROM product_reviews r
    WHERE r.status = true AND r.seller = :seller
""")
    long countTotalReviews(@Param("seller") SellerEntity seller);

    @Query("""
                SELECT COUNT(r)
                FROM product_reviews r
                WHERE r.status = true
                  AND r.seller = :seller
                  AND r.replyContent IS NOT NULL
                  AND r.replyContent <> ''
            """)
    long countRepliedReviews(@Param("seller") SellerEntity seller);

    @Query("""
    SELECT r.vote, COUNT(r)
    FROM product_reviews r
    WHERE r.status = true AND r.seller = :seller
    GROUP BY r.vote
    HAVING r.vote BETWEEN 1 AND 5
    ORDER BY r.vote DESC
""")
    List<Object[]> getRatingDistribution(@Param("seller") SellerEntity seller);


    long countByProductAndStatusTrue(ProductEntity product);

}
