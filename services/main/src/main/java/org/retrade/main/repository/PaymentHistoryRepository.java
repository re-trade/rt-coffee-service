package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.constant.PaymentStatusEnum;
import org.retrade.main.model.entity.CustomerEntity;
import org.retrade.main.model.entity.OrderEntity;
import org.retrade.main.model.entity.PaymentHistoryEntity;
import org.retrade.main.model.entity.PaymentMethodEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentHistoryRepository extends BaseJpaRepository<PaymentHistoryEntity, String> {
    List<PaymentHistoryEntity> findByOrder(OrderEntity order);
    List<PaymentHistoryEntity> findByPaymentMethod(PaymentMethodEntity paymentMethod);
    List<PaymentHistoryEntity> findByPaymentStatus(PaymentStatusEnum paymentStatus);
    Optional<PaymentHistoryEntity> findByPaymentCode(String paymentCode);
    List<PaymentHistoryEntity> findByOrderAndPaymentStatus(OrderEntity order, PaymentStatusEnum paymentStatus);
    List<PaymentHistoryEntity> findByCustomer(CustomerEntity customer);
}
