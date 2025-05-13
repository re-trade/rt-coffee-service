package org.retrade.main.repository;

import org.springframework.stereotype.Repository;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.common.repository.BaseMongoRepository;

import java.util.Optional;

@Repository
public interface AccountRepository extends BaseMongoRepository<AccountEntity, String> {
    Optional<AccountEntity> findByUsername(String username);
}
