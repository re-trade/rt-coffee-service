package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.OrderStatusEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderStatusRepository extends BaseJpaRepository<OrderStatusEntity, String> {
    Optional<OrderStatusEntity> findByCode(String code);
    Optional<OrderStatusEntity> findByName(String name);

    List<OrderStatusEntity> findAllByEnabledTrue();
}
