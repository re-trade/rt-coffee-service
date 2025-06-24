package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.CategoryEntity;
import org.retrade.main.model.entity.OrderEntity;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends BaseJpaRepository<ProductEntity, String> {
    Page<ProductEntity> findBySeller(SellerEntity seller, Pageable pageable);
    List<ProductEntity> findByBrand(String brand);
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

    @Query("SELECT oi.product FROM order_items oi WHERE oi.order = :order")
    List<ProductEntity> findProductsByOrder(@Param("order") OrderEntity order);
}
