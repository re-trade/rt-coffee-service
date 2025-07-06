package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.CustomerEntity;
import org.retrade.main.model.entity.OrderEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends BaseJpaRepository<OrderEntity, String> {
    List<OrderEntity> findByCustomer(CustomerEntity customer);
    List<OrderEntity> findByCustomerAndCreatedDateBetween(CustomerEntity customer, LocalDateTime startDate, LocalDateTime endDate);

    // o day ne cai query k thi o product review cung duoc nhu nhau
}
