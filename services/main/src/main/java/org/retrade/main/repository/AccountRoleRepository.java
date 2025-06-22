package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.AccountRoleEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRoleRepository extends BaseJpaRepository<AccountRoleEntity, String> {
}