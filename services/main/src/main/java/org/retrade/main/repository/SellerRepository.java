package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends BaseJpaRepository<SellerEntity, String> {
    Optional<SellerEntity> findByAccount(AccountEntity account);
    boolean existsByIdentityNumberIgnoreCase(@NonNull String identityNumber);
}
