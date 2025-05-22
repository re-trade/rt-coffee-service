package org.retrade.main.controller;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<ResponseObject<AccountResponse>> getMe() {
        AccountResponse response = accountService.getMe();
        return ResponseEntity.ok(new ResponseObject.Builder<AccountResponse>()
                .success(true)
                .code("SUCCESS")
                .content(response)
                .messages("Account retrieved successfully")
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<AccountResponse>> getAccountById(@PathVariable String id) {
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(new ResponseObject.Builder<AccountResponse>()
                .success(true)
                .code("SUCCESS")
                .content(response)
                .messages("Account retrieved successfully")
                .build());
    }

    @PutMapping("/{id}/password")
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

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteAccount(@PathVariable String id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Account deleted successfully")
                .build());
    }

    @GetMapping
    public ResponseEntity<ResponseObject<List<AccountResponse>>> getAllAccounts(@PageableDefault(size = 10, page = 0) Pageable page, @RequestParam String q) {
        PaginationWrapper<List<AccountResponse>> response = accountService.getAllAccounts(QueryWrapper.builder().search(q).pageable(page).build());
        return ResponseEntity.ok(new ResponseObject.Builder<List<AccountResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(response)
                .messages("Accounts retrieved successfully")
                .build());
    }
}
