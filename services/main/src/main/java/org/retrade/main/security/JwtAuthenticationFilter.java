package org.retrade.main.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.retrade.main.model.constant.JwtTokenType;
import org.retrade.main.service.JwtService;
import org.retrade.main.service.impl.UserDetailServiceImpl;
import org.retrade.main.util.TokenUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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
            if (userClaims.isPresent()) {
                var claims = userClaims.get();
                var userDetails = userDetailService.loadUserByUsername(claims.getUsername());
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
