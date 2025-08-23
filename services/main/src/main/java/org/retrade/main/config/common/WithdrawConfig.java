package org.retrade.main.config.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Data
@Configuration
@ConfigurationProperties(prefix = "withdraw")
public class WithdrawConfig {
    private BigDecimal minWithdraw;
    private BigDecimal maxWithdraw;
    private BigDecimal dailyLimit;
    private int maxPendingRequest;
}
