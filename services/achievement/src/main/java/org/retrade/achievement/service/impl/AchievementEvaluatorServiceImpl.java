package org.retrade.achievement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.achievement.client.OrderServiceClient;
import org.retrade.achievement.client.TokenServiceClient;
import org.retrade.achievement.model.constant.ConditionTypeCode;
import org.retrade.achievement.model.entity.AchievementEntity;
import org.retrade.achievement.model.entity.SellerAchievementEntity;
import org.retrade.achievement.model.message.EmailNotificationMessage;
import org.retrade.achievement.repository.AchievementConditionRepository;
import org.retrade.achievement.repository.SellerAchievementRepository;
import org.retrade.achievement.service.AchievementEvaluatorService;
import org.retrade.achievement.service.MessageProducerService;
import org.retrade.common.model.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementEvaluatorServiceImpl implements AchievementEvaluatorService {
    private final AchievementConditionRepository achievementConditionRepository;
    private final SellerAchievementRepository sellerAchievementRepository;
    private final MessageProducerService messageProducerService;
    private final OrderServiceClient orderServiceClient;
    private final TokenServiceClient tokenServiceClient;

    @Override
    public void evaluate(String sellerId, String type) {
        var totalOrders = orderServiceClient.getOrderCount(sellerId);
        var achievementUnlocked = new ArrayList<AchievementEntity>();
        var conditions = achievementConditionRepository.findByType(type);
        conditions.forEach(condition -> {
            boolean achieved = false;
            double progress = 0.0;

            if (Objects.equals(type, ConditionTypeCode.BECOME_SELLER)) {
                achieved = true;
                progress = 1.0;

            } else if (Objects.equals(type, ConditionTypeCode.ORDER_COMPLETED)) {
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
                achievementUnlocked.add(condition.getAchievement());
                log.info("Achievement unlocked for seller {}: {}", sellerId, condition.getAchievement().getCode());
            }
            sellerAchievementRepository.save(sa);
        });
        if (!achievementUnlocked.isEmpty()) {
            var sellerAccount = tokenServiceClient.getSellerProfileBySellerId(sellerId);
            if (!sellerAccount.getIsValid()) {
                throw new ValidationException("Seller account is not valid");
            }
            var seller = sellerAccount.getUserInfo();
            Map<String, Object> templateVariables = Map.of(
                    "sellerName", seller.getSellerName(),
                    "sellerId", seller.getSellerId(),
                    "achievements", achievementUnlocked.stream()
                            .map(ach -> Map.of(
                                    "name", ach.getName(),
                                    "description", ach.getDescription(),
                                    "icon", ach.getIcon()
                            )).toList()
            );

            messageProducerService.sendEmailNotification(EmailNotificationMessage.builder()
                            .to(seller.getEmail())
                    .subject("ACHIEVEMENT UNLOCKED")
                            .templateName("achievement-unlocked")
                            .templateVariables(templateVariables)
                            .retryCount(0)
                            .messageId(UUID.randomUUID().toString())
                            .templateVariables(Map.of())
                    .build());
        }
    }

}
