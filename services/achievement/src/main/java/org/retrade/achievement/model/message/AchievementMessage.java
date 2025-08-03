package org.retrade.achievement.model.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementMessage <T> implements Serializable {
    private String sellerId;
    private String eventType;
    private T payload;
}