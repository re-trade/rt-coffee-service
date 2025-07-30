package org.retrade.achievement.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AchievementRequest {
    private String code;
    private String name;
    private String description;
    private String icon;
    private Boolean isActivated;
}
