package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.CustomerContactEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerContactRepository extends BaseJpaRepository<CustomerContactEntity, String> {
}
