package org.retrade.main.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.CustomerEntity;
import org.retrade.main.model.entity.OrderEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends BaseJpaRepository<OrderEntity, String> {
    List<OrderEntity> findByCustomer(CustomerEntity customer);
    List<OrderEntity> findByCustomerAndCreatedDateBetween(CustomerEntity customer, LocalDateTime startDate, LocalDateTime endDate);
    @Query("""
    SELECT od.order
    FROM order_combos oc
    JOIN oc.orderDestination od
    WHERE oc.id = :orderComboId
    """)
    Optional<OrderEntity> findOrderByOrderComboId(@Param("orderComboId") String orderComboId);
}
