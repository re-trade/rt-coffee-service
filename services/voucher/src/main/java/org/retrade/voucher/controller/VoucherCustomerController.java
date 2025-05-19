package org.retrade.voucher.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.voucher.model.dto.request.ApplyVoucherRequest;
import org.retrade.voucher.model.dto.request.ClaimVoucherRequest;
import org.retrade.voucher.model.dto.request.ValidateVoucherRequest;
import org.retrade.voucher.model.dto.response.VoucherClaimResponse;
import org.retrade.voucher.model.dto.response.VoucherValidationResponse;
import org.retrade.voucher.service.VoucherClaimService;
import org.retrade.voucher.service.VoucherValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class VoucherCustomerController {
    private final VoucherClaimService voucherClaimService;
    private final VoucherValidationService voucherValidationService;

    @PostMapping("/claim")
    public ResponseEntity<ResponseObject<VoucherClaimResponse>> claimVoucher(
            @Valid @RequestBody ClaimVoucherRequest request) {
        VoucherClaimResponse response = voucherClaimService.claimVoucher(request);
        return ResponseEntity.ok(new ResponseObject.Builder<VoucherClaimResponse>()
                .success(true)
                .code("VOUCHER_CLAIMED")
                .messages("Voucher claimed successfully")
                .content(response)
                .build());
    }

    @GetMapping("/user/{accountId}")
    public ResponseEntity<ResponseObject<List<VoucherClaimResponse>>> getUserVouchers(
            @PathVariable String accountId) {
        List<VoucherClaimResponse> responses = voucherClaimService.getUserVouchers(accountId);
        return ResponseEntity.ok(new ResponseObject.Builder<List<VoucherClaimResponse>>()
                .success(true)
                .code("USER_VOUCHERS_FOUND")
                .content(responses)
                .build());
    }

    @GetMapping("/user/{accountId}/active")
    public ResponseEntity<ResponseObject<List<VoucherClaimResponse>>> getUserActiveVouchers(
            @PathVariable String accountId) {
        List<VoucherClaimResponse> responses = voucherClaimService.getUserActiveVouchers(accountId);
        return ResponseEntity.ok(new ResponseObject.Builder<List<VoucherClaimResponse>>()
                .success(true)
                .code("USER_ACTIVE_VOUCHERS_FOUND")
                .content(responses)
                .build());
    }

    @PostMapping("/validate")
    public ResponseEntity<ResponseObject<VoucherValidationResponse>> validateVoucher(
            @Valid @RequestBody ValidateVoucherRequest request) {
        VoucherValidationResponse response = voucherValidationService.validateVoucher(request);
        return ResponseEntity.ok(new ResponseObject.Builder<VoucherValidationResponse>()
                .success(response.isValid())
                .code(response.isValid() ? "VOUCHER_VALID" : "VOUCHER_INVALID")
                .messages(response.getMessage())
                .content(response)
                .build());
    }

    @PostMapping("/apply")
    public ResponseEntity<ResponseObject<VoucherValidationResponse>> applyVoucher(
            @Valid @RequestBody ApplyVoucherRequest request) {
        VoucherValidationResponse response = voucherValidationService.applyVoucher(request);
        return ResponseEntity.ok(new ResponseObject.Builder<VoucherValidationResponse>()
                .success(response.isValid())
                .code(response.isValid() ? "VOUCHER_APPLIED" : "VOUCHER_APPLICATION_FAILED")
                .messages(response.getMessage())
                .content(response)
                .build());
    }
}
