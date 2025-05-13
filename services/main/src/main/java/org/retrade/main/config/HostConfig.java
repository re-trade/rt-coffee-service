package org.retrade.main.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "security.host")
public class HostConfig {
    private String originAllows;
    private String baseHost;
}
