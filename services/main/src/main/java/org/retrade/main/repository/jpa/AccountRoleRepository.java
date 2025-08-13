package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.AccountRoleEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


@Repository
public interface AccountRoleRepository extends BaseJpaRepository<AccountRoleEntity, String> {
    @Query("SELECT ar FROM account_roles ar WHERE ar.account.id = :accountId AND ar.role.id = :roleId")
    Optional<AccountRoleEntity> findByAccountIdAndRoleId(@Param("accountId") String accountId, @Param("roleId") String roleId);
}