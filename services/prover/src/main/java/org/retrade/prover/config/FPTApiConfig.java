package org.retrade.prover.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fpt")
@Getter
public class FPTApiConfig {
    private String baseUrl;
    private String apiKey;
}
