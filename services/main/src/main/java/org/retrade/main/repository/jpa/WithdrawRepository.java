package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.constant.WithdrawStatusEnum;
import org.retrade.main.model.entity.WithdrawRequestEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface WithdrawRepository extends BaseJpaRepository<WithdrawRequestEntity, String> {
    @Query("SELECT COALESCE(SUM(w.amount), 0) " +
            "FROM withdraw_requests w " +
            "WHERE w.account.id = :accountId " +
            "AND w.createdDate BETWEEN :startOfDay AND :endOfDay " +
            "AND w.status <> :excludedStatus")
    BigDecimal sumAmountByAccountAndDate(@Param("accountId") String accountId,
                                         @Param("startOfDay") LocalDateTime startOfDay,
                                         @Param("endOfDay") LocalDateTime endOfDay,
                                         @Param("excludedStatus") WithdrawStatusEnum excludedStatus);

    @Query("SELECT COUNT(w) " +
            "FROM withdraw_requests w " +
            "WHERE w.account.id = :accountId " +
            "AND w.status = :status")
    long countByAccountAndStatus(@Param("accountId") String accountId,
                                 @Param("status") WithdrawStatusEnum status);
}
