package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.DeliveryTrackEntity;
import org.retrade.main.model.entity.OrderComboEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryTrackRepository extends BaseJpaRepository<DeliveryTrackEntity, String> {
    List<DeliveryTrackEntity> findBySeller(SellerEntity seller);
    List<DeliveryTrackEntity> findByOrderCombo(OrderComboEntity orderCombo);
    List<DeliveryTrackEntity> findByStatus(Boolean status);
    List<DeliveryTrackEntity> findBySellerAndOrderCombo(SellerEntity seller, OrderComboEntity orderCombo);
}
