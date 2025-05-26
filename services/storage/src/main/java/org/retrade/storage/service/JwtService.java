package org.retrade.storage.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.retrade.storage.model.constant.JwtTokenType;
import org.retrade.storage.model.other.UserClaims;
import org.springframework.security.core.Authentication;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

public interface JwtService {
    String generateToken(Authentication authentication, JwtTokenType tokenType);

    Claims generateClaims(UserClaims claimInfo);

    String generateToken(String username, List<String> role, JwtTokenType tokenType);

    String generateToken(UserClaims userClaims);

    Optional<UserClaims> getUserClaimsFromJwt(String token, JwtTokenType tokenType);

    Optional<UserClaims> getUserClaimsFromJwt(EnumMap<JwtTokenType, Cookie> cookieEnumMap);

    Cookie tokenCookieWarp(String token, JwtTokenType tokenType);

    void removeAuthToken (HttpServletRequest request, HttpServletResponse response);

    void removeAuthToken(Cookie cookie, HttpServletResponse response);

    boolean isTokenValid(String token, JwtTokenType tokenType);
}
