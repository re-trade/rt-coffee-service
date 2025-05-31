package org.retrade.main.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.config.ForgotPasswordConfig;
import org.retrade.main.config.GoogleApisConfig;
import org.retrade.main.model.constant.JwtTokenType;
import org.retrade.main.model.dto.request.AuthenticationRequest;
import org.retrade.main.model.dto.request.ForgotPasswordRequest;
import org.retrade.main.model.dto.response.AuthResponse;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.CustomerEntity;
import org.retrade.main.model.entity.LoginSessionEntity;
import org.retrade.main.model.message.EmailNotificationMessage;
import org.retrade.main.model.other.GoogleProfileResponse;
import org.retrade.main.repository.AccountRepository;
import org.retrade.main.repository.LoginSessionRepository;
import org.retrade.main.service.AuthService;
import org.retrade.main.service.JwtService;
import org.retrade.main.service.MessageProducerService;
import org.retrade.main.util.AuthUtils;
import org.retrade.main.util.QRUtils;
import org.retrade.main.util.TokenUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthUtils authUtils;
    private final AccountRepository accountRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final GoogleApisConfig googleApisConfig;
    private final ForgotPasswordConfig forgotPasswordConfig;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final MessageProducerService messageProducerService;

    @Override
    public AuthResponse localAuthentication(AuthenticationRequest authenticationRequest, HttpServletRequest request) {
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
        saveSession(request, account);
        return AuthResponse.builder()
                .tokens(tokenMap)
                .roles(authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .twoFA(account.isUsing2FA())
                .build();
    }

    @Override
    public AuthResponse localAuthentication(AuthenticationRequest authenticationRequest, HttpServletRequest request, Consumer<List<Cookie>> callback) {
        var result =  localAuthentication(authenticationRequest, request);
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
        var otpText = TokenUtils.generateOTPValue(account.getSecret(), account.getUsername(), "ReTrade");
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

    @Override
    public String generateGoogleAuthenticationUrl() {
        return new GoogleAuthorizationCodeRequestUrl(
                googleApisConfig.getClientId(),
                googleApisConfig.getRedirectUrl(),
                googleApisConfig.getScopes().stream().map(scope -> String.format("https://www.googleapis.com/auth/userinfo.%s", scope)).toList()
        ).setState("/profile").build();
    }

    @Override
    public void forgotPasswordUrlCreate(String email) {
        var accountEntity = accountRepository.findByEmail(email).orElseThrow(() -> new ValidationException("Not found account with this email"));
        var token = UUID.randomUUID().toString();
        var url =  String.format("%s?token=%s",forgotPasswordConfig.getCallbackUrl(), token);
        redisTemplate.opsForValue().set(String.format("%s:%s", "user:forgot-password", token), email,forgotPasswordConfig.getTimeout(), TimeUnit.MINUTES);
        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put("url", url);
        templateVars.put("timeOut", forgotPasswordConfig.getTimeout());
        templateVars.put("username", accountEntity.getUsername());
        EmailNotificationMessage emailMessage = EmailNotificationMessage.builder()
                .to(accountEntity.getEmail())
                .subject("Forgot Password")
                .templateName("forgot-password")
                .templateVariables(templateVars)
                .messageId(UUID.randomUUID().toString())
                .retryCount(0)
                .build();
        messageProducerService.sendEmailNotification(emailMessage);
    }

    @Override
    public void forgotPasswordConfirm(ForgotPasswordRequest request) {
        if (!request.getPassword().equals(request.getRePassword())) {
            throw new ValidationException("Passwords do not match");
        }
        String email = (String) redisTemplate.opsForValue().get(String.format("%s:%s", "user:forgot-password", request.getToken()));
        if (email == null) {
            throw new ValidationException("Token Not Valid");
        }
        var accountEntity = accountRepository.findByEmail(email).orElseThrow(() -> new ValidationException("Not found account with this email"));
        accountEntity.setHashPassword(passwordEncoder.encode(request.getPassword()));
        accountRepository.save(accountEntity);
    }

    @Override
    public AuthResponse googleOAuth2Callback(String code, Consumer<List<Cookie>> callback) {
        AtomicReference<AuthResponse> authResponse = new AtomicReference<>();
        try {
            GoogleTokenResponse response = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(), new GsonFactory(),
                    googleApisConfig.getClientId(), googleApisConfig.getClientSecret(),
                    code, googleApisConfig.getRedirectUrl())
                    .execute();
            var googleProfile = fetchGoogleProfile(response.getAccessToken());
            var email = googleProfile.getEmail();
            accountRepository.findByEmail(email).ifPresentOrElse((account) -> {
                authResponse.set(wrapAccountToAuthResponse(account));
            }, ()-> {
                var customer = CustomerEntity.builder()
                        .firstName(googleProfile.getFamilyName())
                        .lastName(googleProfile.getGivenName())
                        .address("")
                        .phone("")
                        .avatarUrl(googleProfile.getPicture())
                        .build();
                var accountEntity = AccountEntity.builder()
                        .email(email)
                        .username(generateGoogleUsername(email))
                        .hashPassword("")
                        .secret(TokenUtils.generateSecretKey())
                        .enabled(true)
                        .locked(false)
                        .using2FA(false)
                        .joinInDate(LocalDateTime.now())
                        .customer(customer)
                        .build();
                var account = accountRepository.save(accountEntity);
                authResponse.set(wrapAccountToAuthResponse(account));
            });
            List<Cookie> cookies = new ArrayList<>();
            cookies.add(jwtService.tokenCookieWarp(authResponse.get().getTokens().get(JwtTokenType.ACCESS_TOKEN), JwtTokenType.ACCESS_TOKEN));
            cookies.add(jwtService.tokenCookieWarp(authResponse.get().getTokens().get(JwtTokenType.REFRESH_TOKEN), JwtTokenType.REFRESH_TOKEN));
            callback.accept(cookies);
            return authResponse.get();
        } catch (Exception e) {
            throw new ActionFailedException(e.getMessage());
        }
    }

    private GoogleProfileResponse fetchGoogleProfile (String accessToken) {
        HttpTransport httpTransport = new NetHttpTransport();
        GenericUrl genericUrl = new GenericUrl(googleApisConfig.getUserProfileEndpoint());
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
        try {
            HttpRequest request = requestFactory.buildGetRequest(genericUrl);
            request.getHeaders().setAuthorization("Bearer " + accessToken);
            HttpResponse response = request.execute();
            if (response.getStatusCode() == HttpStatusCodes.STATUS_CODE_OK) {
                var profileJson = response.parseAsString();
                var gson = new Gson();
                return gson.fromJson(profileJson, GoogleProfileResponse.class);
            }
            throw new ActionFailedException("Problem with connect to google");
        } catch (IOException ex) {
            throw new ActionFailedException(ex.getMessage());
        }
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            jwtService.removeAuthToken(request,response);
        }
    }

    private void saveSession (HttpServletRequest request, AccountEntity account) {
        String deviceFingerprint = request.getHeader("x-device-fingerprint");
        String deviceName = request.getHeader("x-device-name");
        String ipAddress = request.getHeader("x-ip-address");
        String location = request.getHeader("x-location");
        String userAgent = request.getHeader("user-agent");
        LoginSessionEntity loginSession = LoginSessionEntity.builder()
                .account(account)
                .deviceFingerprint(deviceFingerprint != null ? deviceFingerprint : "unknown")
                .deviceName(deviceName != null ? deviceName : "unknown")
                .ipAddress(ipAddress != null ? ipAddress : "unknown")
                .location(location != null ? location : "unknown")
                .userAgent(userAgent != null ? userAgent : "unknown")
                .loginTime(Timestamp.valueOf(Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .build();
        try {
            loginSessionRepository.save(loginSession);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to save login session");
        }
    }

    private AuthResponse wrapAccountToAuthResponse(AccountEntity account) {
        var roles = AuthUtils.convertAccountToRole(account);
        var tokenMap = new EnumMap<JwtTokenType,String>(JwtTokenType.class);
        if (!account.isUsing2FA()) {
            var accessToken = jwtService.generateToken(account.getUsername(), roles, JwtTokenType.ACCESS_TOKEN);
            var refreshToken = jwtService.generateToken(account.getUsername(), roles, JwtTokenType.REFRESH_TOKEN);
            tokenMap.put(JwtTokenType.ACCESS_TOKEN, accessToken);
            tokenMap.put(JwtTokenType.REFRESH_TOKEN, refreshToken);
        } else {
            var twoFAToken = jwtService.generateToken(account.getUsername(), roles, JwtTokenType.TWO_FA_TOKEN);
            tokenMap.put(JwtTokenType.TWO_FA_TOKEN, twoFAToken);
        }
        return AuthResponse.builder()
                .tokens(tokenMap)
                .roles(roles)
                .twoFA(account.isUsing2FA())
                .build();
    }

    private String generateGoogleUsername(String email) {
        return String.format("google:%s", email);
    }

}
