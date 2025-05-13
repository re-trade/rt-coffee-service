package org.retrade.main.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.main.model.dto.request.AuthenticationRequest;
import org.retrade.main.model.dto.request.ExternalCustomerAccountAuthRequest;
import org.retrade.main.model.dto.response.AuthResponse;
import org.retrade.main.service.AuthService;
import org.retrade.common.model.dto.response.ResponseObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "auth")
public class AuthenticationController {
    private final AuthService authService;

    @PostMapping("register/2fa")
    public ResponseEntity<BufferedImage> signupTwoFactorAuth (@RequestParam(name = "width", defaultValue = "300") int width,@RequestParam(name = "height", defaultValue = "300") int height) {
        var result = authService.register2FaAuthentication(width, height);
        return ResponseEntity.ok(result);
    }
    @PatchMapping("2fa/verify")
    public ResponseEntity<ResponseObject<Map<String, Boolean>>> verifyTwoFactorAuth(@RequestParam(name = "code", defaultValue = "") String code) {
        var result = authService.verify2FaAuthentication(code);
        return ResponseEntity.ok(new ResponseObject.Builder<Map<String, Boolean>>()
                        .success(true)
                        .code("OTP_VALIDATE_SUCCESS")
                        .messages("OTP_VALIDATE_SUCCESS")
                        .content(Map.of("code", result))
                .build());
    }
    @PostMapping("local")
    public ResponseEntity<ResponseObject<AuthResponse>> customerAuthenticationInternal(@Valid @RequestBody AuthenticationRequest request, HttpServletResponse response) {
        var result = authService.localAuthentication(request, (cookies -> {
            cookies.forEach(response::addCookie);
        } ));
        return ResponseEntity.ok(new ResponseObject.Builder<AuthResponse>()
                        .code("AUTH_SUCCESS")
                        .success(true)
                        .messages("Authentication successful")
                        .content(result)
                        .build()
        );
    }
    @PostMapping("external")
    public ResponseEntity<?> customerAuthenticationExternal(@Valid @RequestBody ExternalCustomerAccountAuthRequest request) {
        return null;
    }
}
