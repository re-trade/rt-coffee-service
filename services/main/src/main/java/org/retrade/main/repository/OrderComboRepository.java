package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.OrderComboEntity;
import org.retrade.main.model.entity.OrderDestinationEntity;
import org.retrade.main.model.entity.OrderStatusEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderComboRepository extends BaseJpaRepository<OrderComboEntity, String> {
    List<OrderComboEntity> findBySeller(SellerEntity seller);
    List<OrderComboEntity> findByOrderDestination(OrderDestinationEntity orderDestination);
    List<OrderComboEntity> findByOrderStatus(OrderStatusEntity orderStatus);
    List<OrderComboEntity> findBySellerAndOrderStatus(SellerEntity seller, OrderStatusEntity orderStatus);
}
