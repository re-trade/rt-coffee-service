package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.OrderComboDeliveryEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderComboDeliveryRepository extends BaseJpaRepository<OrderComboDeliveryEntity, String> {
}
