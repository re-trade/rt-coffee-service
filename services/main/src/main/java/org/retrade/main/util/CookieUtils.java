package org.retrade.main.util;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.retrade.main.model.constant.JwtTokenType;

import java.util.Arrays;
import java.util.EnumMap;


public class CookieUtils {
    public static Cookie[] getCookies(@Nonnull HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies == null) {
            return new Cookie[0];
        }
        return cookies;
    }
    public static EnumMap<JwtTokenType, Cookie> getCookieMap(@Nonnull HttpServletRequest request) {
        EnumMap<JwtTokenType, Cookie> cookieMap = new EnumMap<>(JwtTokenType.class);
        Arrays.stream(CookieUtils.getCookies(request))
                .filter(CookieUtils::validateValidCookie)
                .forEach(cookie -> cookieMap.put(JwtTokenType.valueOf(cookie.getName().toUpperCase()), cookie));
        return cookieMap;
    }
    public static boolean validateValidCookie(@Nonnull Cookie cookie) {
        String cookieName = cookie.getName();
        String cookieValue = cookie.getValue();
        if (cookieName == null || cookieValue == null || cookieValue.isEmpty()) {
            return false;
        }
        try {
            JwtTokenType.valueOf(cookieName.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
