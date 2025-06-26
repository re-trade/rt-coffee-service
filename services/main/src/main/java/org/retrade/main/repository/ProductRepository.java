package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.CategoryEntity;
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
    Page<ProductEntity> findBySeller(SellerEntity seller, Pageable pageable);
    List<ProductEntity> findByNameContainingIgnoreCase(String name);
    Page<ProductEntity> findByVerified(Boolean verified, Pageable pageable);

    @Query("SELECT p FROM products p JOIN p.categories c WHERE c = :category")
    List<ProductEntity> findByCategory(@Param("category") CategoryEntity category);

    @Query("SELECT p FROM products p JOIN p.categories c WHERE c = :category")
    Page<ProductEntity> findByCategory(@Param("category") CategoryEntity category, Pageable pageable);

    @Query("SELECT p FROM products p JOIN p.categories c WHERE c.name = :categoryName")
    List<ProductEntity> findByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT p FROM products p JOIN p.categories c WHERE c.name = :categoryName")
    Page<ProductEntity> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

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




//    @Query("SELECT AVG(p.avgVote) FROM products p WHERE p.seller = :seller AND p.avgVote > 0")
//    Double findAverageRatingBySeller(@Param("seller") SellerEntity seller);
}
