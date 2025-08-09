package org.retrade.feedback_notification.config.security;

import lombok.RequiredArgsConstructor;
import org.retrade.feedback_notification.security.CustomAuthenticationEntryPoint;
import org.retrade.feedback_notification.security.JwtAuthenticationFilter;
import org.retrade.feedback_notification.security.JwtCookieAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final CorsConfig corsConfig;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter;
    @Bean
    SecurityFilterChain authenticationFilterChain (HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()));
        http.authorizeHttpRequests((auth) -> auth.requestMatchers("/api-docs/**", "/swagger-ui/**", "/actuator/health")
                .permitAll()
                .requestMatchers("/files/**").permitAll()
                .anyRequest().authenticated());
        http.exceptionHandling(exception -> {
            exception.authenticationEntryPoint(customAuthenticationEntryPoint);
        });
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAfter(this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(this.jwtCookieAuthenticationFilter, JwtAuthenticationFilter.class);
        return http.build();
    }
}
