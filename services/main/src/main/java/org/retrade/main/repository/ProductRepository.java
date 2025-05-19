package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends BaseJpaRepository<ProductEntity, String> {
    List<ProductEntity> findBySeller(SellerEntity seller);
    List<ProductEntity> findByBrand(String brand);
    List<ProductEntity> findByNameContainingIgnoreCase(String name);
}
