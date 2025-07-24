package org.retrade.achievement.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
