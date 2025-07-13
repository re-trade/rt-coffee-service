package org.retrade.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.WithdrawRequest;
import org.retrade.main.model.dto.response.AccountWalletResponse;
import org.retrade.main.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("withdraw")
    public ResponseEntity<ResponseObject<Void>> withdrawRequest(@Valid @RequestBody WithdrawRequest request) {
        walletService.withdrawRequest(request);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("WITHDRAW_SUBMIT")
                .messages("Withdraw submit successfully")
                .build());
    }

    @PostMapping("withdraw/{id}/approve")
    public ResponseEntity<ResponseObject<Void>> withdrawApproved(@PathVariable String id) {
        walletService.approveWithdrawRequest(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("WITHDRAW_APPROVED")
                .messages("Withdraw approved successfully")
                .build());
    }

    @GetMapping("me/balance")
    public ResponseEntity<ResponseObject<AccountWalletResponse>> getAccountBalance() {
        var result = walletService.getUserAccountWallet();
        return ResponseEntity.ok(new ResponseObject.Builder<AccountWalletResponse>()
                .success(true)
                .code("WALLET_RETRIEVED")
                .content(result)
                .messages("Wallet retrieved successfully")
                .build());
    }
}
