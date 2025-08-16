package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.CustomerContactEntity;
import org.retrade.main.model.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface CustomerContactRepository extends BaseJpaRepository<CustomerContactEntity, String> {
    Collection<CustomerContactEntity> findByCustomerAndDefaulted(CustomerEntity customer, Boolean defaulted);
}
