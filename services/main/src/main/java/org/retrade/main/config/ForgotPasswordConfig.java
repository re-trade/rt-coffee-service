package org.retrade.main.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "security.forgot-password")
public class ForgotPasswordConfig {
    private Long timeout;
    private String callbackUrl;
}
