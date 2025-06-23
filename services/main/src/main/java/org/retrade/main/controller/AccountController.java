package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.UpdatePasswordRequest;
import org.retrade.main.model.dto.response.AccountResponse;
import org.retrade.main.service.AccountService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
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
    public ResponseEntity<ResponseObject<AccountResponse>> getAccountById(@PathVariable String id) {
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(new ResponseObject.Builder<AccountResponse>()
                .success(true)
                .code("SUCCESS")
                .content(response)
                .messages("Account retrieved successfully")
                .build());
    }

<<<<<<< HEAD
    @PutMapping("{id}/password")
=======
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

    @PutMapping("password")
>>>>>>> 644b29bb29325c5f33ef47e964a21213396462ca
    public ResponseEntity<ResponseObject<Void>> updatePassword(
            @PathVariable String id,
            @Valid @RequestBody UpdatePasswordRequest request) {
        accountService.updatePassword(id, request);
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
}
