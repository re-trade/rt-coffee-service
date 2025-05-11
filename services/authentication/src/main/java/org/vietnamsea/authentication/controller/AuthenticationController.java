package org.vietnamsea.authentication.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vietnamsea.authentication.model.dto.request.AuthenticationRequest;
import org.vietnamsea.authentication.model.dto.request.ExternalCustomerAccountAuthRequest;
import org.vietnamsea.authentication.model.dto.response.AuthResponse;
import org.vietnamsea.authentication.service.AuthService;
import org.vietnamsea.common.model.dto.response.ResponseObject;

import java.awt.image.BufferedImage;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "auth")
public class AuthenticationController {
    private final AuthService authService;

    @PostMapping("register/2fa")
    public ResponseEntity<BufferedImage> signupTwoFactorAuth (@RequestParam(name = "width", value = "300") int width,@RequestParam(name = "height", value = "300") int height) {
        var result = authService.register2FaAuthentication(width, height);
        return ResponseEntity.ok(result);
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
