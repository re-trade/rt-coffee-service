package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.TransactionTypeEnum;
import org.retrade.main.model.constant.WithdrawStatusEnum;
import org.retrade.main.model.dto.request.VietQrGenerateRequest;
import org.retrade.main.model.dto.request.WithdrawRequest;
import org.retrade.main.model.dto.response.AccountWalletResponse;
import org.retrade.main.model.dto.response.BankResponse;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.TransactionEntity;
import org.retrade.main.model.entity.VietQrBankEntity;
import org.retrade.main.model.entity.WithdrawRequestEntity;
import org.retrade.main.repository.*;
import org.retrade.main.service.VietQRService;
import org.retrade.main.service.WalletService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final AuthUtils authUtils;
    private final AccountRepository accountRepository;
    private final CustomerBankInfoRepository customerBankInfoRepository;
    private final TransactionRepository transactionRepository;
    private final WithdrawRepository withdrawRepository;
    private final VietQrBankRepository vietQrBankRepository;
    private final VietQRService vietQRService;


    @Override
    public AccountWalletResponse getUserAccountWallet() {
        var account = authUtils.getUserAccountFromAuthentication();
        return wrapAccountWalletResponse(account);
    }

    @Override
    public void withdrawRequest(WithdrawRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (request.getAmount().compareTo(account.getBalance()) > 0) {
            throw new ValidationException("Insufficient balance");
        }
        var bankInfo = customerBankInfoRepository.findById(request.getBankProfileId()).orElseThrow(() -> new ValidationException("Bank profile not found"));
        var withdraw = WithdrawRequestEntity.builder()
                .account(account)
                .amount(request.getAmount())
                .status(WithdrawStatusEnum.PENDING)
                .bankAccount(bankInfo.getAccountNumber())
                .bankBin(bankInfo.getBankBin())
                .userBankName(bankInfo.getUserBankName())
                .build();

        var vietQr = VietQrGenerateRequest.builder()
                .accountNo(bankInfo.getAccountNumber())
                .accountName(bankInfo.getUserBankName())
                .acqId(bankInfo.getBankBin())
                .addInfo(request.getContent())
                .amount(request.getAmount().longValue())
                .template("compact2")
                .build();
        var qrCode = vietQRService.generateQr(vietQr);
        withdraw.setQrCodeUrl(qrCode);
        try {
            withdrawRepository.save(withdraw);
        } catch (Exception ex) {
            throw new ActionFailedException("Have a problem when save withdraw request", ex);
        }
    }

    @Transactional
    @Override
    public void approveWithdrawRequest(String withdrawRequestId) {
        var withdraw = withdrawRepository.findById(withdrawRequestId).orElseThrow(() -> new ValidationException("Withdraw request not found"));
        var account = withdraw.getAccount();
        if (account.getBalance().compareTo(withdraw.getAmount()) < 0) {
            throw new ValidationException("Insufficient balance");
        }
        updateBalance(account.getBalance().subtract(withdraw.getAmount()), account);
        var tx = TransactionEntity.builder()
                .account(account)
                .amount(withdraw.getAmount().negate())
                .type(TransactionTypeEnum.WITHDRAW)
                .build();
        transactionRepository.save(tx);
        withdraw.setStatus(WithdrawStatusEnum.COMPLETED);
        withdraw.setProcessedDate(new Timestamp(System.currentTimeMillis()));
        withdrawRepository.save(withdraw);
    }

    @Override
    public PaginationWrapper<List<BankResponse>> getBankList(QueryWrapper queryWrapper) {
        var result = vietQrBankRepository.search(queryWrapper);
        var list = result.map(this::wrapBankResponse).stream().toList();
        return new PaginationWrapper.Builder<List<BankResponse>>()
                .setData(list)
                .setPaginationInfo(result)
                .build();
    }

    private void updateBalance(BigDecimal balance, AccountEntity account) {
        account.setBalance(balance);
        try {
            accountRepository.save(account);
        } catch (Exception ex) {
            throw new ActionFailedException("Have a problem when update balance", ex);
        }
    }

    private BankResponse wrapBankResponse(VietQrBankEntity bankEntity) {
        return BankResponse.builder()
                .id(bankEntity.getId())
                .name(bankEntity.getName())
                .code(bankEntity.getCode())
                .bin(bankEntity.getBin())
                .url(bankEntity.getLogo())
                .build();
    }

    public AccountWalletResponse wrapAccountWalletResponse(AccountEntity accountEntity) {
        return AccountWalletResponse.builder()
                    .accountId(accountEntity.getId())
                    .balance(accountEntity.getBalance())
                    .build();
    }
}
