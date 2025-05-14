package org.retrade.main.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.main.model.dto.request.AuthenticationRequest;
import org.retrade.main.model.dto.request.ExternalCustomerAccountAuthRequest;
import org.retrade.main.model.dto.response.AuthResponse;
import org.retrade.main.service.AuthService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "auth")
public class AuthenticationController {
    private final AuthService authService;

    @PostMapping("register/2fa")
    public ResponseEntity<byte[]> signupTwoFactorAuth(
            @RequestParam(name = "width", defaultValue = "300") int width,
            @RequestParam(name = "height", defaultValue = "300") int height) {

        BufferedImage result = authService.register2FaAuthentication(width, height);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(result, "png", baos);
            baos.flush();
            byte[] imageBytes = baos.toByteArray();
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (IOException e) {
            throw new ActionFailedException("Failed to register 2fa authentication", e);
        }
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
