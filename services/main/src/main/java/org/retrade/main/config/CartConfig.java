package org.retrade.main.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cart")
public class CartConfig {
    private Long ttlDays = 30L;
    private Integer maxItemsPerCart = 100;
    private Integer maxQuantityPerItem = 99;
    private String keyPrefix = "cart:user:";
}
