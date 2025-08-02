package org.retrade.achievement.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessagePayload {
    private String orderId;
    private BigDecimal amount;
    private LocalDateTime completedAt;
}
