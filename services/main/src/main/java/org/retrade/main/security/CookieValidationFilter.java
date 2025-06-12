package org.retrade.main.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.retrade.main.model.constant.JwtTokenType;
import org.retrade.main.service.JwtService;
import org.retrade.main.util.CookieUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.EnumMap;


@Component
@RequiredArgsConstructor
public class CookieValidationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        EnumMap<JwtTokenType, Cookie> cookieMap = CookieUtils.getCookieMap(request);
        cookieMap.forEach((name, value) -> {
            if (!jwtService.isTokenValid(value.getValue(), name)) {
                jwtService.removeAuthToken(value, response);
            }
        });
        filterChain.doFilter(request, response);
    }
}
