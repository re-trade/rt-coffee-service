package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.TransactionEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends BaseJpaRepository<TransactionEntity, String> {
}
