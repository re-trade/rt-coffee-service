package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.CustomerAccountRegisterRequest;
import org.retrade.main.model.dto.response.CustomerAccountRegisterResponse;
import org.retrade.main.service.RegisterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("registers")
@RequiredArgsConstructor
@Tag(name = "Registration", description = "User registration endpoints")
public class RegisterController {
    private final RegisterService registerService;

    @Operation(
            summary = "Register new customer account",
            description = "Create a new customer account with profile information. " +
                    "This endpoint does not require authentication and will automatically send welcome email."
    )
    @PostMapping(path = "customers/account")
    public ResponseEntity<ResponseObject<CustomerAccountRegisterResponse>> customerRegisterAccount (@Valid @RequestBody CustomerAccountRegisterRequest request) {
        var result = registerService.customerRegister(request);
        return ResponseEntity.ok(new ResponseObject.Builder<CustomerAccountRegisterResponse>()
                        .success(true)
                        .code("SUCCESS")
                        .content(result)
                        .messages("Tạo tài khoản thành công")
                .build());
    }
}
