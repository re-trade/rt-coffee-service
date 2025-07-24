package org.retrade.achievement.repository;

import org.retrade.achievement.model.entity.AchievementConditionEntity;
import org.retrade.common.repository.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface AchievementConditionRepository extends BaseJpaRepository<AchievementConditionEntity, String> {
    Collection<AchievementConditionEntity> findByType(String type);
}
