package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderComboRepository extends BaseJpaRepository<OrderComboEntity, String> {
    List<OrderComboEntity> findBySeller(SellerEntity seller);
    List<OrderComboEntity> findByOrderDestination(OrderDestinationEntity orderDestination);
    List<OrderComboEntity> findByOrderStatus(OrderStatusEntity orderStatus);
    List<OrderComboEntity> findBySellerAndOrderStatus(SellerEntity seller, OrderStatusEntity orderStatus);
    List<OrderComboEntity> findByOrderItems_Order_Id(String orderId);

    boolean existsByOrderDestination_Order_CustomerAndId(@NonNull CustomerEntity customer, @NonNull String id);

    Optional<OrderComboEntity> findByIdAndSeller(String id, SellerEntity seller);
    Optional<OrderComboEntity> findByIdAndOrderDestination(String id, OrderDestinationEntity orderDestination);
}
