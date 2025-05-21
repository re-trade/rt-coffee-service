package org.retrade.main.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.retrade.main.model.dto.request.AuthenticationRequest;
import org.retrade.main.model.dto.request.ForgotPasswordRequest;
import org.retrade.main.model.dto.response.AuthResponse;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Consumer;

public interface AuthService {
    AuthResponse localAuthentication(AuthenticationRequest authenticationRequest, HttpServletRequest request);

    AuthResponse localAuthentication(AuthenticationRequest authenticationRequest, HttpServletRequest request, Consumer<List<Cookie>> callback);

    BufferedImage register2FaAuthentication(int width, int height);

    boolean verify2FaAuthentication(String totp);

    String generateGoogleAuthenticationUrl();

    void forgotPasswordUrlCreate(String email);

    void forgotPasswordConfirm(ForgotPasswordRequest request);

    AuthResponse googleOAuth2Callback(String code, Consumer<List<Cookie>> callback);

    void logout(HttpServletRequest request, HttpServletResponse response);
}
