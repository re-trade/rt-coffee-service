package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.WithdrawRequestEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface WithdrawRepository extends BaseJpaRepository<WithdrawRequestEntity, String> {
}
