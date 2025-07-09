package org.retrade.main.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.dto.response.TopCustomerResponse;
import org.retrade.main.model.dto.response.TopSellersResponse;
import org.retrade.main.model.entity.CustomerEntity;
import org.retrade.main.model.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends BaseJpaRepository<OrderEntity, String> {
    List<OrderEntity> findByCustomer(CustomerEntity customer);
    List<OrderEntity> findByCustomerAndCreatedDateBetween(CustomerEntity customer, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT o. " +
            "FROM orders o JOIN o.orderHistories oh " +
            "WHERE oh.seller.id = :sellerId " +
            "GROUP BY o.customer " +
            "ORDER BY COUNT(o) DESC LIMIT 10")
    List<CustomerEntity> findTopCustomerOrdersBySeller(@Param("sellerId") String sellerId);

    @Query("SELECT COUNT(o) " +
            "FROM orders o JOIN o.orderHistories oh " +
            "WHERE o.customer.id = :customerId AND oh.seller.id = :sellerId")
    long countOrdersByCustomerAndSeller(@Param("customerId") String customerId, @Param("sellerId") String sellerId);



}
