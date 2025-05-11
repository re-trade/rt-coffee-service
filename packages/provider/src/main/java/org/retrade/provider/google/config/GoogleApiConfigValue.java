package org.retrade.provider.google.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "google")
public class GoogleApiConfigValue {
    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private List<String> scopes;
    private String userProfileEndpoint;
}
