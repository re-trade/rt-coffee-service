package org.retrade.feedback_notification.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.retrade.feedback_notification.config.security.HostConfig;
import org.retrade.feedback_notification.config.security.JwtConfig;
import org.retrade.feedback_notification.model.constant.JwtTokenType;
import org.retrade.feedback_notification.model.other.UserClaims;
import org.retrade.feedback_notification.service.JwtService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.sql.Date;
import java.util.EnumMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtConfig jwtTokenConfig;
    private final HostConfig hostConfig;

    @Override
    public Claims generateClaims(UserClaims claimInfo) {
        Claims claims = Jwts.claims();
        claims.put("user", claimInfo);
        return claims;
    }

    private SecretKey getSigningKey(JwtTokenType tokenType) {
        String secretKey = switch (tokenType) {
            case ACCESS_TOKEN -> jwtTokenConfig.getAccessToken().getKey();
        };
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    @Override
    public Optional<UserClaims> getUserClaimsFromJwt(String token, JwtTokenType tokenType) {
        try {
            var claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(tokenType))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            ObjectMapper objectMapper = new ObjectMapper();
            String userJson = objectMapper.writeValueAsString(claims.get("user"));
            return Optional.of(objectMapper.readValue(userJson, UserClaims.class));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserClaims> getUserClaimsFromJwt(EnumMap<JwtTokenType, Cookie> cookieEnumMap) {
        return cookieEnumMap.entrySet().stream()
                .map(entry -> getUserClaimsFromJwt(entry.getValue().getValue(), entry.getKey()))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    private Date getExpiryDate(JwtTokenType tokenType, Date currentDate) {
        long duration = switch (tokenType) {
            case ACCESS_TOKEN -> jwtTokenConfig.getAccessToken().getMaxAge();
        };
        return new Date(currentDate.getTime() + duration);
    }
}
