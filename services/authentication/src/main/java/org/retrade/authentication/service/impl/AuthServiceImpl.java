package org.retrade.authentication.service.impl;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.retrade.authentication.model.constant.JwtTokenType;
import org.retrade.authentication.model.dto.request.AuthenticationRequest;
import org.retrade.authentication.model.dto.response.AuthResponse;
import org.retrade.authentication.repository.AccountRepository;
import org.retrade.authentication.service.AuthService;
import org.retrade.authentication.service.JwtService;
import org.retrade.authentication.util.AuthUtils;
import org.retrade.authentication.util.QRUtils;
import org.retrade.authentication.util.TokenUtils;
import org.retrade.common.model.exception.ValidationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthUtils authUtils;
    private final AccountRepository accountRepository;

    @Override
    public AuthResponse localAuthentication(AuthenticationRequest authenticationRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        var account = authUtils.getUserAccountFromAuthentication();
        var tokenMap = new EnumMap<JwtTokenType,String>(JwtTokenType.class);
        if (!account.isUsing2FA()) {
            var accessToken = jwtService.generateToken(authentication, JwtTokenType.ACCESS_TOKEN);
            var refreshToken = jwtService.generateToken(authentication, JwtTokenType.REFRESH_TOKEN);
            tokenMap.put(JwtTokenType.ACCESS_TOKEN, accessToken);
            tokenMap.put(JwtTokenType.REFRESH_TOKEN, refreshToken);
        } else {
            var twoFAToken = jwtService.generateToken(authentication, JwtTokenType.TWO_FA_TOKEN);
            tokenMap.put(JwtTokenType.TWO_FA_TOKEN, twoFAToken);
        }
        return AuthResponse.builder()
                .tokens(tokenMap)
                .roles(authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .twoFA(account.isUsing2FA())
                .build();
    }

    @Override
    public AuthResponse localAuthentication(AuthenticationRequest authenticationRequest, Consumer<List<Cookie>> callback) {
        var result =  localAuthentication(authenticationRequest);
        var cookieList = new ArrayList<Cookie>();
        var tokens = result.getTokens();
        if (!result.isTwoFA()) {
            var accessTokenCookie = jwtService.tokenCookieWarp(tokens.get(JwtTokenType.ACCESS_TOKEN), JwtTokenType.ACCESS_TOKEN);
            var refreshTokenCookie = jwtService.tokenCookieWarp(tokens.get(JwtTokenType.REFRESH_TOKEN), JwtTokenType.REFRESH_TOKEN);
            cookieList.addAll(List.of(accessTokenCookie, refreshTokenCookie));
        } else {
            var twoFATokenCookie = jwtService.tokenCookieWarp(tokens.get(JwtTokenType.TWO_FA_TOKEN), JwtTokenType.TWO_FA_TOKEN);
            cookieList.add(twoFATokenCookie);
        }
        callback.accept(cookieList);
        return result;
    }
    
    @Override
    public BufferedImage register2FaAuthentication(int width, int height) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.isUsing2FA()) {
            throw new ValidationException("This user is using 2FA authentication now");
        }
        var otpText = TokenUtils.generateOTPValue(account.getSecret(), account.getUsername(), "VietnamSea");
        return QRUtils.generateQRCode(otpText, width, height);
    }
    
    @Override
    public boolean verify2FaAuthentication(String totp) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (!account.isUsing2FA()) {
            account.setUsing2FA(true);
            accountRepository.save(account);
        }
        var totpSystem = TokenUtils.getTOTPCode(account.getSecret());
        return totpSystem.equals(totp);
    }
}
