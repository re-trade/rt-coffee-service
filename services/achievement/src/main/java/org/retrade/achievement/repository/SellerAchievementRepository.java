package org.retrade.achievement.repository;

import org.retrade.achievement.model.entity.SellerAchievementEntity;
import org.retrade.common.repository.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerAchievementRepository extends BaseJpaRepository<SellerAchievementEntity, String> {
}
