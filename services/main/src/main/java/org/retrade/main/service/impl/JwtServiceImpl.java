package org.retrade.main.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.retrade.main.config.security.HostConfig;
import org.retrade.main.config.security.JwtConfig;
import org.retrade.main.model.constant.JwtTokenType;
import org.retrade.main.model.other.UserClaims;
import org.retrade.main.service.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.sql.Date;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtConfig jwtTokenConfig;
    private final HostConfig hostConfig;
    @Override
    public String generateToken(Authentication authentication, JwtTokenType tokenType) {
        UserDetails user = (UserDetails) authentication.getPrincipal();
        var roles = user.getAuthorities().toArray(new GrantedAuthority[0]);
        return generateToken(user.getUsername(), Arrays.stream(roles).map(GrantedAuthority::getAuthority).toList(),tokenType);
    }
    @Override
    public Claims generateClaims(UserClaims claimInfo) {
        Claims claims = Jwts.claims();
        claims.put("user", claimInfo);
        return claims;
    }
    @Override
    public String generateToken(String username, List<String> role, JwtTokenType tokenType) {
        Date currentDate = new Date(System.currentTimeMillis());
        Date expiryDate = getExpiryDate(tokenType, currentDate);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(currentDate)
                .setClaims(generateClaims(UserClaims.builder()
                        .username(username)
                        .roles(role)
                        .tokenType(tokenType)
                        .build()))
                .setExpiration(expiryDate)
                .signWith(getSigningKey(tokenType))
                .compact();
    }



    @Override
    public String generateToken(UserClaims userClaims) {
        return generateToken(userClaims.getUsername(),userClaims.getRoles(),userClaims.getTokenType());
    }
    private SecretKey getSigningKey(JwtTokenType tokenType) {
        String secretKey = switch (tokenType) {
            case ACCESS_TOKEN -> jwtTokenConfig.getAccessToken().getKey();
            case REFRESH_TOKEN -> jwtTokenConfig.getRefreshToken().getKey();
            case TWO_FA_TOKEN -> jwtTokenConfig.getTwofaToken().getKey();
        };
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
    @Override
    public Optional<UserClaims> getUserClaimsFromJwt(String token, JwtTokenType tokenType) {
        try {
            var claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(tokenType))
                    .build()
                    .parseClaimsJws(token).getBody();
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
    @Override
    public Cookie tokenCookieWarp(String token, JwtTokenType tokenType) {
        long jwtEx = switch (tokenType) {
            case ACCESS_TOKEN -> jwtTokenConfig.getAccessToken().getMaxAge();
            case REFRESH_TOKEN -> jwtTokenConfig.getRefreshToken().getMaxAge();
            case TWO_FA_TOKEN -> jwtTokenConfig.getTwofaToken().getMaxAge();
        };

        var cookie = new Cookie(tokenType.toString(), token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge((int) (jwtEx));
        cookie.setAttribute("SameSite", "None");
        if (!hostConfig.getBaseHost().isEmpty() && !hostConfig.getDevelopMode()) {
            cookie.setDomain(hostConfig.getBaseHost());
        }
        return cookie;
    }
    @Override
    public void removeAuthToken(HttpServletRequest request, HttpServletResponse response) {
        var cookieRequest = request.getCookies();
        if (cookieRequest == null) {
            return;
        }
        var authCookie = Arrays.stream(cookieRequest).filter(cookie -> cookie.getName().equals(JwtTokenType.ACCESS_TOKEN.toString()) || cookie.getName().equals(JwtTokenType.REFRESH_TOKEN.name()) ).toList();
        authCookie.forEach(cookie -> {
            removeAuthToken(cookie, response);
        });
    }

    @Override
    public void removeAuthToken(Cookie cookie, HttpServletResponse response) {
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setValue("");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "None");
        if (!hostConfig.getBaseHost().isEmpty() && !hostConfig.getDevelopMode()) {
            cookie.setDomain(hostConfig.getBaseHost());
        }
        response.addCookie(cookie);
    }

    @Override
    public boolean isTokenValid(String token, JwtTokenType tokenType) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(tokenType))
                    .build()
                    .parseClaimsJws(token);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
    
    private Date getExpiryDate(JwtTokenType tokenType, Date currentDate) {
        Date expiryDate = null;
        if(tokenType == JwtTokenType.ACCESS_TOKEN) {
            expiryDate = new Date(currentDate.getTime() + jwtTokenConfig.getAccessToken().getMaxAge());
        }else if (tokenType == JwtTokenType.REFRESH_TOKEN) {
            expiryDate = new Date(currentDate.getTime() + jwtTokenConfig.getRefreshToken().getMaxAge());
        }else if (tokenType == JwtTokenType.TWO_FA_TOKEN) {
            expiryDate = new Date(currentDate.getTime() + jwtTokenConfig.getTwofaToken().getMaxAge());
        }
        return expiryDate;
    }
}
