package org.retrade.main.service;

import jakarta.servlet.http.Cookie;
import org.retrade.main.model.dto.request.AuthenticationRequest;
import org.retrade.main.model.dto.response.AuthResponse;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Consumer;

public interface AuthService {
    AuthResponse localAuthentication(AuthenticationRequest authenticationRequest);

    AuthResponse localAuthentication(AuthenticationRequest authenticationRequest, Consumer<List<Cookie>> callback);

    BufferedImage register2FaAuthentication(int width, int height);

    boolean verify2FaAuthentication(String totp);
}
