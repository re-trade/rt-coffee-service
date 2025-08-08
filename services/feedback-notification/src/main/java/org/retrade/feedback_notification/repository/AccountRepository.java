package org.retrade.feedback_notification.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.feedback_notification.model.entity.AccountEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends BaseJpaRepository<AccountEntity, String> {
    Optional<AccountEntity> findByAccountId(String accountId);

    Optional<AccountEntity> findByUsername(String username);
}
