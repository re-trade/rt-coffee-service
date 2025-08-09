package org.retrade.feedback_notification.util;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import org.retrade.feedback_notification.model.other.UserClaims;
import org.retrade.feedback_notification.service.impl.UserDetailServiceImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.util.Optional;

public class UserClaimUtils {
    public static void handleUserClaim(@Nonnull HttpServletRequest request, Optional<UserClaims> userClaims, UserDetailServiceImpl userDetailService) {
        if (userClaims.isPresent()) {
            var claims = userClaims.get();
            var userDetails = userDetailService.loadUserByUsername(claims.getUsername());
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
    }
}
