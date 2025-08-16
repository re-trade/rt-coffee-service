package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.OrderComboDeliveryEntity;
import org.retrade.main.model.entity.OrderComboEntity;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface OrderComboDeliveryRepository extends BaseJpaRepository<OrderComboDeliveryEntity, String> {
    Collection<OrderComboDeliveryEntity> findByOrderCombo(OrderComboEntity orderCombo);
}
