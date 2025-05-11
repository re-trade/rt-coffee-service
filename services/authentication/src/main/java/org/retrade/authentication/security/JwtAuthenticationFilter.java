package org.retrade.authentication.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.retrade.authentication.model.constant.JwtTokenType;
import org.retrade.authentication.service.JwtService;
import org.retrade.authentication.service.impl.UserDetailServiceImpl;
import org.retrade.authentication.util.CookieUtils;

import java.io.IOException;
import java.util.EnumMap;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserDetailServiceImpl userDetailService;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain filterChain) throws ServletException, IOException {
        EnumMap<JwtTokenType, Cookie> cookieMap = CookieUtils.getCookieMap(request);
        var userClaims = jwtService.getUserClaimsFromJwt(cookieMap);
        if (userClaims.isPresent()) {
            var claims = userClaims.get();
            var userDetails = userDetailService.loadUserByUsername(claims.getUsername());
            if (claims.getTokenType() == JwtTokenType.REFRESH_TOKEN) {
                var newAccessToken = jwtService.generateToken(claims.getUsername(), claims.getRoles(), JwtTokenType.ACCESS_TOKEN);
                var newAccessCookie = jwtService.tokenCookieWarp(newAccessToken, JwtTokenType.ACCESS_TOKEN);
                response.addCookie(newAccessCookie);
            }
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(request, response);
    }
}
