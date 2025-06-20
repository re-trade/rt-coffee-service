package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.AuthException;
import org.retrade.main.config.HostConfig;
import org.retrade.main.model.dto.request.AuthenticationRequest;
import org.retrade.main.model.dto.request.ForgotPasswordRequest;
import org.retrade.main.model.dto.request.ResetPasswordRequest;
import org.retrade.main.model.dto.response.AccountResponse;
import org.retrade.main.model.dto.response.AuthResponse;
import org.retrade.main.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthenticationController {
    private final AuthService authService;
    private final HostConfig hostConfig;

    @Operation(
            summary = "Generate 2FA QR Code",
            description = "Generate a QR code for setting up Two-Factor Authentication. Requires authentication.",
            security = {@SecurityRequirement(name = "bearerAuth"), @SecurityRequirement(name = "cookieAuth")}
    )
    @PostMapping("register/2fa")
    public ResponseEntity<byte[]> signupTwoFactorAuth(
            @Parameter(description = "QR code width in pixels") @RequestParam(name = "width", defaultValue = "300") int width,
            @Parameter(description = "QR code height in pixels") @RequestParam(name = "height", defaultValue = "300") int height) {

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

    @Operation(
            summary = "Verify 2FA Code",
            description = "Verify the Two-Factor Authentication code from authenticator app. Requires authentication.",
            security = {@SecurityRequirement(name = "bearerAuth"), @SecurityRequirement(name = "cookieAuth")}
    )
    @PatchMapping("2fa/verify")
    public ResponseEntity<ResponseObject<Map<String, Boolean>>> verifyTwoFactorAuth(
            @Parameter(description = "6-digit 2FA code from authenticator app") @RequestParam(name = "code", defaultValue = "") String code) {
        var result = authService.verify2FaAuthentication(code);
        return ResponseEntity.ok(new ResponseObject.Builder<Map<String, Boolean>>()
                        .success(true)
                        .code("OTP_VALIDATE_SUCCESS")
                        .messages("OTP_VALIDATE_SUCCESS")
                        .content(Map.of("code", result))
                .build());
    }

    @Operation(
            summary = "Login with username and password",
            description = "Authenticate user with username/password and return JWT tokens. " +
                    "This endpoint does not require authentication and will set authentication cookies automatically."
    )
    @PostMapping("local")
    public ResponseEntity<ResponseObject<AuthResponse>> authentication(@Valid @RequestBody AuthenticationRequest authenticationRequest, HttpServletRequest request, HttpServletResponse response) {
        var result = authService.localAuthentication(authenticationRequest, request, (cookies -> {
            cookies.forEach(response::addCookie);
        }));
        return ResponseEntity.ok(new ResponseObject.Builder<AuthResponse>()
                        .code("AUTH_SUCCESS")
                        .success(true)
                        .messages("Authentication successful")
                        .content(result)
                        .build()
        );
    }
    @GetMapping("oauth2/google")
    public ResponseEntity<Void> googleOAuthLogin() {
        var url = authService.generateGoogleAuthenticationUrl();
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }
    @GetMapping("oauth2/google/code")
    public ResponseEntity<ResponseObject<AuthResponse>> oauth2Callback (@RequestParam String code, HttpServletResponse response) {
        try {
            var result = authService.googleOAuth2Callback(code, (callback) -> {
                callback.forEach(response::addCookie);
            });
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(hostConfig.getFrontEnd()))
                    .body(new ResponseObject.Builder<AuthResponse>()
                            .code("SUCCESS")
                            .messages("Login Success")
                            .content(result)
                            .success(true)
                            .build());
        } catch (AuthException ex) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(hostConfig.getFrontEnd()))
                    .body(new ResponseObject.Builder<AuthResponse>()
                            .code("FAILED")
                            .messages("Login Failed")
                            .success(true)
                            .build());
        } catch (ActionFailedException ex) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(hostConfig.getFrontEnd()))
                    .body(new ResponseObject.Builder<AuthResponse>()
                            .code("SUCCESS")
                            .messages("Login Failed")
                            .success(true)
                            .build());
        }
    }

    @PostMapping("forgot-password")
    public ResponseEntity<ResponseObject<Void>> forgotPasswordUrlGen(@RequestParam(name = "email") String email) {
        authService.forgotPasswordUrlCreate(email);
        return ResponseEntity.ok(
                new ResponseObject.Builder<Void>()
                        .code("SUCCESS")
                        .messages("Please Check Your Email")
                        .success(true)
                        .build()
        );
    }


    @Operation(
            summary = "Reset password (when logged in)",
            description = "Reset the password by providing old password, new password, and confirm new password. Requires authentication.",
            security = {@SecurityRequirement(name = "bearerAuth"), @SecurityRequirement(name = "cookieAuth")}
    )
    @PutMapping("reset-password")
    public ResponseEntity<ResponseObject<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(
                new ResponseObject.Builder<Void>()
                        .code("SUCCESS")
                        .messages("Password has been reset successfully")
                        .success(true)
                        .build()
        );
    }




    @PostMapping("forgot-password/confirm")
    public ResponseEntity<ResponseObject<Void>> forgotPasswordConfirm(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPasswordConfirm(request);
        return ResponseEntity.ok(
                new ResponseObject.Builder<Void>()
                        .code("SUCCESS")
                        .messages("Change Password Success")
                        .success(true)
                        .build()
        );
    }
}
