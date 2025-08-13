package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.UpdateEmailRequest;
import org.retrade.main.model.dto.request.UpdatePasswordRequest;
import org.retrade.main.model.dto.request.UpdateUsernameRequest;
import org.retrade.main.model.dto.response.AccountDetailResponse;
import org.retrade.main.model.dto.response.AccountResponse;
import org.retrade.main.service.AccountService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "User account management and profile endpoints")
public class AccountController {
    private final AccountService accountService;

    @Operation(
            summary = "Get current user account",
            description = "Retrieve the authenticated user's account information.",
            security = {@SecurityRequirement(name = "bearerAuth"), @SecurityRequirement(name = "cookieAuth")}
    )
    @GetMapping("me")
    public ResponseEntity<ResponseObject<AccountResponse>> getMe() {
        AccountResponse response = accountService.getMe();
        return ResponseEntity.ok(new ResponseObject.Builder<AccountResponse>()
                .success(true)
                .code("SUCCESS")
                .content(response)
                .messages("Account retrieved successfully")
                .build());
    }

    @GetMapping("{id}")
    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<AccountDetailResponse>> getAccountById(@PathVariable String id) {
        var response = accountService.getAccountById(id);
        return ResponseEntity.ok(new ResponseObject.Builder<AccountDetailResponse>()
                .success(true)
                .code("SUCCESS")
                .content(response)
                .messages("Account retrieved successfully")
                .build());
    }

    @Operation(
            summary = "Check if username exists",
            description = "Checks whether a given username already exists in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Username check completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseObject.class),
                            examples = {
                                    @ExampleObject(
                                            name = "username_exists",
                                            summary = "Username exists",
                                            value = """
                        {
                            "success": true,
                            "code": "SUCCESS",
                            "content": {
                                "existed": true
                            },
                            "messages": "Account retrieved successfully"
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "username_not_exists",
                                            summary = "Username doesn't exist",
                                            value = """
                        {
                            "success": true,
                            "code": "SUCCESS",
                            "content": {
                                "existed": false
                            },
                            "messages": "Account retrieved successfully"
                        }
                        """
                                    )
                            }
                    )
            )
    })
    @GetMapping("check-username")
    public ResponseEntity<ResponseObject<Map<String, Boolean>>> checkUsernameExisted(@RequestParam String username) {
        var response = accountService.checkUsernameExisted(username);
        return ResponseEntity.ok(new ResponseObject.Builder<Map<String, Boolean>>()
                .success(true)
                .code("SUCCESS")
                .content(Map.of("existed", response))
                .messages("Account retrieved successfully")
                .build());
    }

    @Operation(
            summary = "Check if email exists",
            description = "Checks whether a given email already exists in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email check completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseObject.class),
                            examples = {
                                    @ExampleObject(
                                            name = "email_exists",
                                            summary = "Email exists",
                                            value = """
                        {
                            "success": true,
                            "code": "SUCCESS",
                            "content": {
                                "existed": true
                            },
                            "messages": "Account retrieved successfully"
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "username_not_exists",
                                            summary = "Username doesn't exist",
                                            value = """
                        {
                            "success": true,
                            "code": "SUCCESS",
                            "content": {
                                "existed": false
                            },
                            "messages": "Account retrieved successfully"
                        }
                        """
                                    )
                            }
                    )
            )
    })
    @GetMapping("check-email")
    public ResponseEntity<ResponseObject<Map<String, Boolean>>> checkEmailExisted(@Valid @RequestParam String email) {
        var response = accountService.checkEmailExisted(email);
        return ResponseEntity.ok(new ResponseObject.Builder<Map<String, Boolean>>()
                .success(true)
                .code("SUCCESS")
                .content(Map.of("existed", response))
                .messages("Account retrieved successfully")
                .build());
    }

    @PutMapping("password")
    public ResponseEntity<ResponseObject<Void>> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request) {
        accountService.updatePassword(request);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Password updated successfully")
                .build());
    }
    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @PatchMapping("customer/{id}/reset-password")
    public ResponseEntity<ResponseObject<Void>> resetAccount(@PathVariable String id) {
        accountService.resetPassword(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                        .success(true)
                        .code("SUCCESS")
                        .messages("Account password reset successfully")
                .build());
    }

    @PatchMapping("email")
    public ResponseEntity<ResponseObject<AccountResponse>> updateEmail(@Valid @RequestBody UpdateEmailRequest request) {
        var response = accountService.updateEmail(request);
        return ResponseEntity.ok(new ResponseObject.Builder<AccountResponse>()
                .success(true)
                .code("SUCCESS")
                .content(response)
                .messages("Account username change successful. Please check your email for the new username.")
                .build());
    }

    @PatchMapping("username")
    public ResponseEntity<ResponseObject<AccountResponse>> updateUsername(
            @Valid @RequestBody UpdateUsernameRequest updateUsernameRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        var result = accountService.updateUsername(updateUsernameRequest,request, response);
        return ResponseEntity.ok(new ResponseObject.Builder<AccountResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Account password reset successfully")
                .build());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ResponseObject<Void>> deleteAccount(@PathVariable String id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Account deleted successfully")
                .build());
    }

    @GetMapping
    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<List<AccountResponse>>> getAllAccounts(@PageableDefault(size = 10, page = 0) Pageable page, @RequestParam(required = false) String q) {
        PaginationWrapper<List<AccountResponse>> response = accountService.getAllAccounts(QueryWrapper.builder().search(q).pageable(page).build());
        return ResponseEntity.ok(new ResponseObject.Builder<List<AccountResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(response)
                .messages("Accounts retrieved successfully")
                .build());
    }

    @PutMapping("{id}/disable-customer")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<Void>> disableCustomer(@PathVariable String id) {
        accountService.disableCustomerAccount(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("ban customer with" + id + "successfully")
                .build());
    }
    @PutMapping("{id}/enable-customer")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<Void>> enableCustomer(@PathVariable String id) {
        accountService.enableCustomerAccount(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("unban customer with" + id + "successfully")
                .build());
    }

}
