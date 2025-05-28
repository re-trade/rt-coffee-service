package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.RoleEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends BaseJpaRepository<RoleEntity, String> {
    Optional<RoleEntity> findByCode(String code);
}
