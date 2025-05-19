package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.ProductPriceHistoryEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductPriceHistoryRepository extends BaseJpaRepository<ProductPriceHistoryEntity, String> {
    List<ProductPriceHistoryEntity> findByProduct(ProductEntity product);
    List<ProductPriceHistoryEntity> findByProductAndFromDateAfter(ProductEntity product, LocalDateTime fromDate);
    List<ProductPriceHistoryEntity> findByProductAndToDateIsNull(ProductEntity product);
}
