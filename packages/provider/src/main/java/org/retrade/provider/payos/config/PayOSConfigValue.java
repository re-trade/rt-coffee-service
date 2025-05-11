package org.retrade.provider.payos.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "payos")
public class PayOSConfigValue {
    private String clientId;
    private String apiKey;
    private String checksumKey;
}
