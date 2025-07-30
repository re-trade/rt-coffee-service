package org.retrade.achievement.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementConditionResponse {
    private String id;
    private String type;
    private Double threshold;
    private Integer periodDays;
}
