package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.dto.response.TopSellersResponse;
import org.retrade.main.model.entity.AccountEntity;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends BaseJpaRepository<AccountEntity, String> {
    Optional<AccountEntity> findByUsername(String username);

    Optional<AccountEntity> findByEmail(String email);

    boolean existsByUsername(String username);
}
