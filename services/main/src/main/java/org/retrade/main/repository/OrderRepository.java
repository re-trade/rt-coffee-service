package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.CustomerEntity;
import org.retrade.main.model.entity.OrderEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends BaseJpaRepository<OrderEntity, String> {
    List<OrderEntity> findByCustomer(CustomerEntity customer);
    List<OrderEntity> findByCustomerAndCreatedDateBetween(CustomerEntity customer, LocalDateTime startDate, LocalDateTime endDate);
}
