package org.retrade.main.config.provider;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "payment.vqr")
public class VietQRConfig {
    private String url;
    private String clientId;
    private String apiKey;
    private String banksUrl;
}
