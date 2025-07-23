package org.retrade.achievement.repository;

import org.retrade.achievement.model.entity.AchievementConditionEntity;
import org.retrade.common.repository.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementConditionRepository extends BaseJpaRepository<AchievementConditionEntity, String> {
}
