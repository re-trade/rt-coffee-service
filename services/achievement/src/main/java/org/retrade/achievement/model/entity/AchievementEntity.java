package org.retrade.achievement.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "achievements")
public class AchievementEntity extends BaseSQLEntity {
    @Column(name = "code", nullable = false, unique = true)
    private String code;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "icon", nullable = false)
    private String icon;
    @Column(name = "activated", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isActivated;
}
