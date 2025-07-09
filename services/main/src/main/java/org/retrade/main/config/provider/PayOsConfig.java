package org.retrade.main.config.provider;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Getter
@Configuration
public class PayOsConfig {
    @Value("${payment.payos.client-id}")
    private String clientId;
    @Value("${payment.payos.api-key}")
    private String apiKey;
    @Value("${payment.payos.checksum-key}")
    private String checksumKey;

    @Bean
    PayOS payOS() {
        return new PayOS(clientId, apiKey, checksumKey);
    }
}
