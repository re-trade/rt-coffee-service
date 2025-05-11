package org.retrade.provider.aws.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "aws")
public class AWSConfigValue {
    private String region;
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String bucket;
}
