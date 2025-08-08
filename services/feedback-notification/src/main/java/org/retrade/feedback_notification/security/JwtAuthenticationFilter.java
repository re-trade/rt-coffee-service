package org.retrade.feedback_notification.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.feedback_notification.client.TokenServiceClient;
import org.retrade.feedback_notification.util.TokenUtils;
import org.retrade.proto.authentication.TokenType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenServiceClient tokenServiceClient;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain filterChain) throws ServletException, IOException {
        String accessToken = TokenUtils.getTokenFromHeader(request);
        if (accessToken != null) {
            var userClaims = tokenServiceClient.verifyToken(accessToken, TokenType.ACCESS_TOKEN);
            if (userClaims.getIsValid()) {
                var claims = userClaims.getUserInfo();
                var roles = claims.getRolesList().stream().map(SimpleGrantedAuthority::new).toList();
                UserDetails userDetails = User.builder()
                        .username(claims.getUsername())
                        .password("")
                        .authorities(roles)
                        .disabled(!claims.getIsActive())
                        .accountLocked(!claims.getIsVerified())
                        .build();
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
