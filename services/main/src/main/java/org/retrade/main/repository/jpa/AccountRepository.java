package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.AccountEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends BaseJpaRepository<AccountEntity, String> {
    Optional<AccountEntity> findByUsername(String username);

    Optional<AccountEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(@NonNull String email);

    @Query("SELECT COUNT(a) from  accounts a where a.enabled = true ")
    long countAccounts();

    long countByAccountRoles_Role_CodeNot(String code);


    @Query("SELECT COUNT(a) FROM accounts a " +
            "WHERE YEAR(a.createdDate) = :year " +
            "AND MONTH(a.createdDate) = :month")
    long countAccountsCreatedInMonth(@Param("year") int year, @Param("month") int month);

}
