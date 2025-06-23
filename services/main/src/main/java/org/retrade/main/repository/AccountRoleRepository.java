package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.AccountRoleEntity;
<<<<<<< HEAD

public interface AccountRoleRepository extends BaseJpaRepository<AccountRoleEntity, String> {
}
=======
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRoleRepository extends BaseJpaRepository<AccountRoleEntity, String> {
}
>>>>>>> 644b29bb29325c5f33ef47e964a21213396462ca
