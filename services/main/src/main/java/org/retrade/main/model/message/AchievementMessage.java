package org.retrade.main.model.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementMessage <T> {
    private String sellerId;
    private String eventType;
    private T payload;
}
