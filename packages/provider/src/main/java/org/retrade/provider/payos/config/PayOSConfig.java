package org.retrade.provider.payos.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Getter
@Configuration
@RequiredArgsConstructor
public class PayOSConfig {
    private final PayOSConfigValue payOSConfigValue;
    @Bean
    PayOS payOS () {
        return new PayOS(payOSConfigValue.getClientId(), payOSConfigValue.getApiKey(), payOSConfigValue.getChecksumKey());
    }
}
