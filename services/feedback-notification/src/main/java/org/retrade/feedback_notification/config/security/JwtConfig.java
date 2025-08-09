package org.retrade.feedback_notification.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Data
@Configuration
@ConfigurationProperties(prefix = "security.token.jwt")
public class JwtConfig {
    private JwtConfigValue accessToken;
    @Data
    public static class JwtConfigValue {
        private String key;
        private long maxAge;
    }
}
