package org.retrade.achievement.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerAchievementResponse {
    private String id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private Boolean achieved;
    private Double progress;
    private String achievedAt;
}
