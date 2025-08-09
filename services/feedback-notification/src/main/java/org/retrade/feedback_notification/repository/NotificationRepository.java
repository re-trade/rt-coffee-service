package org.retrade.feedback_notification.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.feedback_notification.model.entity.AccountEntity;
import org.retrade.feedback_notification.model.entity.NotificationEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends BaseJpaRepository<NotificationEntity, String> {

    Optional<NotificationEntity> findByIdAndAccount(String id, AccountEntity account);
}
