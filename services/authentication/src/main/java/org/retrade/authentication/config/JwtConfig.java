package org.retrade.authentication.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Data
@Configuration
public class JwtConfig {
    @Value("${access-token.secret-key}")
    private String jwtSecret;

    @Value("${access-token.max-age}")
    private long jwtExpiration;

    @Value("${refresh-token.secret-key}")
    private String jwtRefreshSecret;

    @Value("${refresh-token.max-age}")
    private long jwtRefreshExpiration;
    private String jwtTwo2FASecret;
    private long jwtTwo2FATokenExpiration;
}
