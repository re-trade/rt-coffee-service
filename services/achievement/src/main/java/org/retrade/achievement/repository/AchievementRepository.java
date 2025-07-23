package org.retrade.achievement.repository;

import org.retrade.achievement.model.entity.AchievementEntity;
import org.retrade.common.repository.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementRepository extends BaseJpaRepository<AchievementEntity, String> {
}
