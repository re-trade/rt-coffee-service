package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.CustomerEntity;
import org.retrade.main.model.entity.OrderEntity;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.ProductReviewEntity;
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
}
