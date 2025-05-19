package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends BaseJpaRepository<CustomerEntity, String> {
    Optional<CustomerEntity> findByAccount(AccountEntity account);
}
