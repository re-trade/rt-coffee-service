package org.retrade.feedback_notification.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.retrade.feedback_notification.model.constant.JwtTokenType;
import org.retrade.feedback_notification.service.JwtService;
import org.retrade.feedback_notification.service.impl.UserDetailServiceImpl;
import org.retrade.feedback_notification.util.TokenUtils;
import org.retrade.feedback_notification.util.UserClaimUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserDetailServiceImpl userDetailService;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain filterChain) throws ServletException, IOException {
        String accessToken = TokenUtils.getTokenFromHeader(request);
        if (accessToken != null) {
            var userClaims = jwtService.getUserClaimsFromJwt(accessToken, JwtTokenType.ACCESS_TOKEN);
            UserClaimUtils.handleUserClaim(request, userClaims, userDetailService);
        }
        filterChain.doFilter(request, response);
    }
}
