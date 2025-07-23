package org.retrade.achievement.model.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "achievements")
public class AchievementEntity extends BaseSQLEntity {
    private String code;
    private String name;
    private String description;
    private String icon;
    private Boolean isActivated;
}
