package org.retrade.achievement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.achievement.client.OrderServiceClient;
import org.retrade.achievement.model.constant.ConditionTypeCode;
import org.retrade.achievement.model.entity.SellerAchievementEntity;
import org.retrade.achievement.repository.AchievementConditionRepository;
import org.retrade.achievement.repository.AchievementRepository;
import org.retrade.achievement.repository.SellerAchievementRepository;
import org.retrade.achievement.service.AchievementEvaluatorService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementEvaluatorServiceImpl implements AchievementEvaluatorService {
    private final AchievementRepository achievementRepository;
    private final AchievementConditionRepository achievementConditionRepository;
    private final SellerAchievementRepository sellerAchievementRepository;
    private final OrderServiceClient orderServiceClient;

    @Override
    public void evaluate(String sellerId, String type) {
        var conditions = achievementConditionRepository.findByType(type);
        conditions.forEach(condition -> {
            boolean achieved = false;
            double progress = 0.0;

            if (Objects.equals(type, ConditionTypeCode.BECOME_SELLER)) {
                achieved = true;
                progress = 1.0;

            } else if (Objects.equals(type, ConditionTypeCode.ORDER_COMPLETED)) {
                long totalOrders = orderServiceClient.getOrderCount(sellerId);
                progress = Math.min(1.0, totalOrders / condition.getThreshold());
                achieved = totalOrders >= condition.getThreshold();
            }

            SellerAchievementEntity sa = sellerAchievementRepository
                    .findBySellerIdAndAchievement_Id(condition.getAchievement().getId(), sellerId)
                    .orElseGet(() -> SellerAchievementEntity.builder()
                           .sellerId(sellerId)
                           .achievement(condition.getAchievement())
                           .achieved(false)
                           .progress(0.0)
                           .achievedAt(null)
                           .build());

            sa.setProgress(progress);

            if (achieved && !sa.getAchieved()) {
                sa.setAchieved(true);
                sa.setAchievedAt(LocalDateTime.now());
                log.info("Achievement unlocked for seller {}: {}", sellerId, condition.getAchievement().getCode());
            }
            sellerAchievementRepository.save(sa);
        });
    }

}
