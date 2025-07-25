package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.OrderComboEntity;
import org.retrade.main.model.entity.OrderEntity;
import org.retrade.main.model.entity.OrderItemEntity;
import org.retrade.main.model.entity.ProductEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends BaseJpaRepository<OrderItemEntity, String> {
    List<OrderItemEntity> findByOrder(OrderEntity order);
    List<OrderItemEntity> findByOrderCombo(OrderComboEntity orderCombo);
    List<OrderItemEntity> findByProduct(ProductEntity product);

    boolean existsByProduct_IdAndOrder_Id(@NonNull String id, @NonNull String id1);

    boolean existsByProduct_IdAndOrderCombo_Id(@NonNull String id, @NonNull String id1);

    List<OrderItemEntity> findByOrder_Id(@NonNull String id);
}
