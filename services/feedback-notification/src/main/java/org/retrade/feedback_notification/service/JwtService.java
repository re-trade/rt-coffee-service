package org.retrade.feedback_notification.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.retrade.feedback_notification.model.constant.JwtTokenType;
import org.retrade.feedback_notification.model.other.UserClaims;

import java.util.EnumMap;
import java.util.Optional;

public interface JwtService {

    Claims generateClaims(UserClaims claimInfo);

    Optional<UserClaims> getUserClaimsFromJwt(String token, JwtTokenType tokenType);

    Optional<UserClaims> getUserClaimsFromJwt(EnumMap<JwtTokenType, Cookie> cookieEnumMap);
}
