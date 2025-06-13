package org.retrade.prover.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fpt")
@Getter
@Setter
public class FPTApiConfig {
    private String baseUrl;
    private String apiKey;
}
