package org.retrade.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.UpdatePasswordRequest;
import org.retrade.main.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("system")
@RestController
@RequiredArgsConstructor
public class SystemController {
    private final AccountService accountService;

    @PutMapping("{id}/password")
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
}
