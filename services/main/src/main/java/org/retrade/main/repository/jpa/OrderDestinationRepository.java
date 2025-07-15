package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.OrderDestinationEntity;
import org.retrade.main.model.entity.OrderEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderDestinationRepository extends BaseJpaRepository<OrderDestinationEntity, String> {
    Optional<OrderDestinationEntity> findByOrder(OrderEntity order);
}
