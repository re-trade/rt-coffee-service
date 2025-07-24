package org.retrade.achievement.repository;

import org.retrade.achievement.model.entity.SellerAchievementEntity;
import org.retrade.common.repository.BaseJpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerAchievementRepository extends BaseJpaRepository<SellerAchievementEntity, String> {

    Optional<SellerAchievementEntity> findBySellerIdAndAchievement_Id(@NonNull String sellerId, @NonNull String id);
}
