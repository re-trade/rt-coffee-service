package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.WalletTransactionEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends BaseJpaRepository<WalletTransactionEntity, String> {
}
