package org.retrade.achievement.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "seller_achievements", uniqueConstraints = @UniqueConstraint(columnNames = {"seller_id", "achievement_id"}))
@Entity(name = "seller_achievements")
public class SellerAchievementEntity extends BaseSQLEntity {
    @Column(name = "seller_id", nullable = false)
    private String sellerId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private AchievementEntity achievement;
    @Column(name = "achieved", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean achieved;
    @Column(name = "progress", nullable = false)
    private Double progress;
    @Column(name = "achieved_at")
    private LocalDateTime achievedAt;
}
