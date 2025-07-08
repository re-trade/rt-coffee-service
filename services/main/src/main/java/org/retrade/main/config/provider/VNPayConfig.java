package org.retrade.main.config.provider;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class VNPayConfig {
    @Value("${payment.vnp.pay-url}")
    private String vnpPayUrl;
    @Value("${payment.vnp.tmn-code}")
    private String vnpTmnCode;
    @Value("${payment.vnp.hash-secret}")
    private String vnpHashSecret;
    @Value("${payment.vnp.api-url}")
    private String vnpApiUrl;
}