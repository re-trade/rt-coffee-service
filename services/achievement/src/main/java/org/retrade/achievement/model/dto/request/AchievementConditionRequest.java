package org.retrade.achievement.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AchievementConditionRequest {
    private String type;
    private Double threshold;
    private Integer periodDays;
    private String achievementId;
}
