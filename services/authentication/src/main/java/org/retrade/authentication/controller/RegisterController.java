package org.retrade.authentication.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.authentication.model.dto.request.CustomerAccountRegisterRequest;
import org.retrade.authentication.model.dto.response.CustomerAccountRegisterResponse;
import org.retrade.authentication.service.RegisterService;
import org.retrade.common.model.dto.response.ResponseObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registers")
@RequiredArgsConstructor
public class RegisterController {
    private final RegisterService registerService;
    @PostMapping(path = "/customers/account")
    public ResponseEntity<ResponseObject<CustomerAccountRegisterResponse>> customerRegisterAccount (@Valid @RequestBody CustomerAccountRegisterRequest request) {
        var result = registerService.customerRegister(request);
        return ResponseEntity.ok(new ResponseObject.Builder<CustomerAccountRegisterResponse>()
                        .success(true)
                        .code("SUCCESS")
                        .content(result)
                        .messages("Create account successfully")
                .build());
    }
    @PutMapping(path = "/customers/profile")
    public ResponseEntity<?> customerRegisterProfile () {
        return null;
    }
    @PostMapping(path = "partners/account")
    public ResponseEntity<?> partnerRegisterAccount () {
        return null;
    }
}
