package org.retrade.achievement.util;

import jakarta.servlet.http.HttpServletRequest;

public class TokenUtils {
    public static String getTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
