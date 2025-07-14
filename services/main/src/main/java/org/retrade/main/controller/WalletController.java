package org.retrade.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.WithdrawRequest;
import org.retrade.main.model.dto.response.AccountWalletResponse;
import org.retrade.main.model.dto.response.BankResponse;
import org.retrade.main.service.WalletService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("me/withdraw")
    public ResponseEntity<ResponseObject<List<AccountWalletResponse>>> getAccountWithdrawalHistory(@PageableDefault Pageable pageable, @RequestParam(required = false) String q) {
        var result = walletService.getWithdrawRequestList(QueryWrapper.builder()
                        .wrapSort(pageable)
                        .search(q)
                .build());
        return ResponseEntity.ok(new ResponseObject.Builder<List<AccountWalletResponse>>()
                .code("WITHDRAW_HISTORY_RETRIEVED")
                .success(true)
                .messages("Withdraw history retrieved successfully")
                .unwrapPaginationWrapper(result)
                .build());
    }

    @GetMapping("banks")
    public ResponseEntity<ResponseObject<List<BankResponse>>> getBanks(@PageableDefault Pageable pageable, @RequestParam(required = false) String q) {
        var result = walletService.getBankList(QueryWrapper.builder()
                        .search(q)
                        .pageable(pageable)
                .build());
        return ResponseEntity.ok(new ResponseObject.Builder<List<BankResponse>>()
                .success(true)
                .code("BANK_RETRIEVED")
                .unwrapPaginationWrapper(result)
                .messages("Bank retrieved successfully")
                .build());
    }
}
