package org.retrade.feedback_notification.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.feedback_notification.model.entity.NotificationEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends BaseJpaRepository<NotificationEntity, String> {
}
