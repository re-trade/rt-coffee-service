package org.retrade.achievement.init;

import lombok.RequiredArgsConstructor;
import org.retrade.achievement.model.constant.ConditionTypeCode;
import org.retrade.achievement.model.entity.AchievementConditionEntity;
import org.retrade.achievement.model.entity.AchievementEntity;
import org.retrade.achievement.repository.AchievementRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AchievementInitializer implements CommandLineRunner {
    private final AchievementRepository achievementRepository;
    @Override
    public void run(String... args) {
        if (achievementRepository.count() == 0) {
            List<AchievementEntity> achievements = new ArrayList<>();

            AchievementEntity sellerAchievement = AchievementEntity.builder()
                    .code("SELLER_REGISTER")
                    .name("Bắt đầu hành trình")
                    .description("Hoàn thành đăng ký trở thành người bán.")
                    .icon("mdi-account-check")
                    .isActivated(true)
                    .build();

            sellerAchievement.setConditions(Set.of(
                    AchievementConditionEntity.builder()
                            .achievement(sellerAchievement)
                            .type(ConditionTypeCode.BECOME_SELLER)
                            .threshold(1)
                            .build()
            ));
            achievements.add(sellerAchievement);

            AchievementEntity firstOrder = AchievementEntity.builder()
                    .code("FIRST_ORDER")
                    .name("Đơn hàng đầu tiên")
                    .description("Hoàn thành đơn hàng bán đầu tiên.")
                    .icon("mdi-cart-arrow-down")
                    .isActivated(true)
                    .build();

            firstOrder.setConditions(Set.of(
                    AchievementConditionEntity.builder()
                            .achievement(firstOrder)
                            .type(ConditionTypeCode.ORDER_COMPLETED)
                            .threshold(1)
                            .build()
            ));
            achievements.add(firstOrder);

            AchievementEntity hardWorker = AchievementEntity.builder()
                    .code("HARD_WORKER")
                    .name("Người chăm chỉ")
                    .description("Bán thành công 6 đơn hàng.")
                    .icon("mdi-cart-check")
                    .isActivated(true)
                    .build();

            hardWorker.setConditions(Set.of(
                    AchievementConditionEntity.builder()
                            .achievement(hardWorker)
                            .type(ConditionTypeCode.ORDER_COMPLETED)
                            .threshold(6)
                            .build()
            ));
            achievements.add(hardWorker);

            AchievementEntity popularSeller = AchievementEntity.builder()
                    .code("POPULAR_SELLER")
                    .name("Người bán nổi bật")
                    .description("Bán thành công 10 đơn hàng.")
                    .icon("mdi-star")
                    .isActivated(true)
                    .build();

            popularSeller.setConditions(Set.of(
                    AchievementConditionEntity.builder()
                            .achievement(popularSeller)
                            .type(ConditionTypeCode.ORDER_COMPLETED)
                            .threshold(10)
                            .build()
            ));
            achievements.add(popularSeller);

            AchievementEntity topSeller = AchievementEntity.builder()
                    .code("TOP_SELLER")
                    .name("Người bán hàng đầu")
                    .description("Bán thành công 20 đơn hàng.")
                    .icon("mdi-trophy")
                    .isActivated(true)
                    .build();

            topSeller.setConditions(Set.of(
                    AchievementConditionEntity.builder()
                            .achievement(topSeller)
                            .type(ConditionTypeCode.ORDER_COMPLETED)
                            .threshold(20)
                            .build()
            ));
            achievements.add(topSeller);

            AchievementEntity legendSeller = AchievementEntity.builder()
                    .code("LEGEND_SELLER")
                    .name("Huyền thoại bán hàng")
                    .description("Bán thành công 50 đơn hàng.")
                    .icon("mdi-crown")
                    .isActivated(true)
                    .build();

            legendSeller.setConditions(Set.of(
                    AchievementConditionEntity.builder()
                            .achievement(legendSeller)
                            .type(ConditionTypeCode.ORDER_COMPLETED)
                            .threshold(50)
                            .build()
            ));
            achievements.add(legendSeller);
            achievementRepository.saveAll(achievements);
        }
    }
}
