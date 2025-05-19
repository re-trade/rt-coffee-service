package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.LoginSessionEntity;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface LoginSessionRepository extends BaseJpaRepository<LoginSessionEntity, String> {
    List<LoginSessionEntity> findByAccount(AccountEntity account);
    List<LoginSessionEntity> findByAccountAndLoginTimeAfter(AccountEntity account, Timestamp loginTime);
    List<LoginSessionEntity> findByDeviceFingerprint(String deviceFingerprint);
    List<LoginSessionEntity> findByIpAddress(String ipAddress);
}
