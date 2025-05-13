package org.retrade.main.config;

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
    private JwtConfigValue refreshToken;
    private JwtConfigValue twofaToken;
    @Data
    public static class JwtConfigValue {
        private String key;
        private long maxAge;
    }
}
