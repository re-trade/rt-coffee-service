package org.retrade.feedback_notification.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.retrade.feedback_notification.model.constant.JwtTokenType;
import org.retrade.feedback_notification.service.impl.JwtServiceImpl;
import org.retrade.feedback_notification.service.impl.UserDetailServiceImpl;
import org.retrade.feedback_notification.util.CookieUtils;
import org.retrade.feedback_notification.util.UserClaimUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.EnumMap;

@Component
@RequiredArgsConstructor
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {
    private final UserDetailServiceImpl userDetailService;
    private final JwtServiceImpl jwtService;
    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,@Nonnull FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        EnumMap<JwtTokenType, Cookie> cookieMap = CookieUtils.getCookieMap(request);
        var userClaims = jwtService.getUserClaimsFromJwt(cookieMap);
        UserClaimUtils.handleUserClaim(request, userClaims, userDetailService);
        filterChain.doFilter(request, response);
    }
}
