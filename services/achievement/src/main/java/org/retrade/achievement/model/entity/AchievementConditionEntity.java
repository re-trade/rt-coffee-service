package org.retrade.achievement.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "achievement_conditions")
public class AchievementConditionEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = AchievementEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private AchievementEntity achievement;
    @Column(nullable = false)
    private String type;
    @Column(nullable = false)
    private double threshold;
    private Integer periodDays;
}
