package org.retrade.achievement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {
    private final HostConfig hostConfig;

    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @SuppressWarnings("null")
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(getCorsAllowed())
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .exposedHeaders("*");
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(getCorsAllowed()));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Accept",
                "Access-Control-Allow-Headers",
                "Access-Control-Allow-Methods",
                "Access-Control-Allow-Origin",
                "Authorization",
                "Content-Type",
                "Origin",
                "X-Requested-With",
                "x-device-fingerprint",
                "x-device-name",
                "x-ip-address",
                "x-location"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList(
                "Accept",
                "Access-Control-Allow-Headers",
                "Access-Control-Allow-Methods",
                "Access-Control-Allow-Origin",
                "Authorization",
                "Content-Type",
                "Origin",
                "X-Requested-With",
                "x-device-fingerprint",
                "x-device-name",
                "x-ip-address",
                "x-location"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private String[] getCorsAllowed() {
        List<String> corsAllowOrigins = new ArrayList<>();
        corsAllowOrigins.add("http://localhost:3000");
        var origin = hostConfig.getOriginAllows().split(",");
        if (origin.length > 0) {
            corsAllowOrigins.addAll(Arrays.asList(origin));
        }
        return corsAllowOrigins.toArray(new String[0]);
    }
}
