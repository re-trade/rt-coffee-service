package org.retrade.achievement.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "seller_achievements")
public class SellerAchievementEntity extends BaseSQLEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private AchievementEntity achievement;

    @Column(nullable = false)
    private boolean achieved;

    @Column(nullable = false)
    private double progress;

    private LocalDateTime achievedAt;
}
