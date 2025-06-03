package org.retrade.voucher.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.voucher.model.dto.request.ClaimVoucherRequest;
import org.retrade.voucher.model.dto.response.VoucherClaimSimpleResponse;
import org.retrade.voucher.service.VoucherClaimService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class VoucherCustomerController {
    private final VoucherClaimService voucherClaimService;

    @PostMapping("claim")
    public ResponseEntity<ResponseObject<VoucherClaimSimpleResponse>> claimVoucher(
            @Valid @RequestBody ClaimVoucherRequest request) {
        VoucherClaimSimpleResponse response = voucherClaimService.claimVoucherSimple(request);
        return ResponseEntity.ok(new ResponseObject.Builder<VoucherClaimSimpleResponse>()
                .success(true)
                .code("VOUCHER_CLAIMED")
                .messages("Voucher claimed successfully")
                .content(response)
                .build());
    }

    @GetMapping("user/{accountId}")
    public ResponseEntity<ResponseObject<List<VoucherClaimSimpleResponse>>> getUserVouchers(
            @PathVariable String accountId) {
        List<VoucherClaimSimpleResponse> responses = voucherClaimService.getUserVouchersSimple(accountId);
        return ResponseEntity.ok(new ResponseObject.Builder<List<VoucherClaimSimpleResponse>>()
                .success(true)
                .code("USER_VOUCHERS_FOUND")
                .content(responses)
                .build());
    }

    @GetMapping("user/{accountId}/active")
    public ResponseEntity<ResponseObject<List<VoucherClaimSimpleResponse>>> getUserActiveVouchers(
            @PathVariable String accountId) {
        List<VoucherClaimSimpleResponse> responses = voucherClaimService.getUserActiveVouchersSimple(accountId);
        return ResponseEntity.ok(new ResponseObject.Builder<List<VoucherClaimSimpleResponse>>()
                .success(true)
                .code("USER_ACTIVE_VOUCHERS_FOUND")
                .content(responses)
                .build());
    }

}
