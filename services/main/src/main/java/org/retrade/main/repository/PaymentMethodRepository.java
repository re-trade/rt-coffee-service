package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.PaymentMethodEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends BaseJpaRepository<PaymentMethodEntity, String> {
    Optional<PaymentMethodEntity> findByCode(String code);
    List<PaymentMethodEntity> findByEnabled(Boolean enabled);
    List<PaymentMethodEntity> findByType(String type);
}
