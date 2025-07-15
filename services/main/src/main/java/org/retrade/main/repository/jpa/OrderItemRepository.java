package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.OrderComboEntity;
import org.retrade.main.model.entity.OrderEntity;
import org.retrade.main.model.entity.OrderItemEntity;
import org.retrade.main.model.entity.ProductEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends BaseJpaRepository<OrderItemEntity, String> {
    List<OrderItemEntity> findByOrder(OrderEntity order);
    List<OrderItemEntity> findByOrderCombo(OrderComboEntity orderCombo);
    List<OrderItemEntity> findByProduct(ProductEntity product);
}
