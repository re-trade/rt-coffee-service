package org.retrade.main.config;

import lombok.RequiredArgsConstructor;
import org.retrade.main.security.CookieValidationFilter;
import org.retrade.main.security.CustomAuthenticationEntryPoint;
import org.retrade.main.security.JwtAuthenticationFilter;
import org.retrade.main.security.JwtCookieAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CookieValidationFilter cookieValidationFilter;
    private final JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter;
    private final CorsConfig corsConfig;
    @Bean
    SecurityFilterChain authenticationFilterChain (HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .authorizeHttpRequests((auth) -> {
                   auth.requestMatchers("/auth/**", "/api-docs/**", "/swagger-ui/**","/actuator/health",
                           "/registers/**",
                           "/passwords/**",
                           "/pings/**","/accounts/check-username"
                           ).permitAll()
                           .requestMatchers(HttpMethod.GET, "/products/**", "/categories/**", "/payments/**")
                           .permitAll()
                           .anyRequest().authenticated();
                }).exceptionHandling(exception -> {
                    exception.authenticationEntryPoint(customAuthenticationEntryPoint);
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(this.cookieValidationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(this.jwtAuthenticationFilter, CookieValidationFilter.class)
                .addFilterAfter(this.jwtCookieAuthenticationFilter, JwtAuthenticationFilter.class);
        return http.build();
    }
}
