package org.retrade.main.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
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
import org.retrade.main.model.dto.response.DecodedFile;
import org.retrade.main.model.dto.response.WithdrawRequestBaseResponse;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.TransactionEntity;
import org.retrade.main.model.entity.VietQrBankEntity;
import org.retrade.main.model.entity.WithdrawRequestEntity;
import org.retrade.main.repository.jpa.AccountRepository;
import org.retrade.main.repository.jpa.CustomerBankInfoRepository;
import org.retrade.main.repository.jpa.TransactionRepository;
import org.retrade.main.repository.jpa.WithdrawRepository;
import org.retrade.main.repository.redis.VietQrBankRepository;
import org.retrade.main.service.VietQRService;
import org.retrade.main.service.WalletService;
import org.retrade.main.util.AuthUtils;
import org.retrade.main.util.HashUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    @Override
    public PaginationWrapper<List<WithdrawRequestBaseResponse>> getWithdrawRequestList(QueryWrapper queryWrapper) {
        return withdrawRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items)  -> {
            var map = vietQrBankRepository.getBankMap();
            var list = items.map(item ->this.wrapWithdrawRequestBaseResponse(item, map)).stream().toList();
            return new PaginationWrapper.Builder<List<WithdrawRequestBaseResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    public BankResponse getBankByBin(String id) {
        var result = vietQrBankRepository.getBankByBin(id);
        if (result.isPresent()) {
            return wrapBankResponse(result.get());
        }
        throw new ValidationException("Bank not found for ID: " + id);
    }

    @Override
    public DecodedFile getQrCodeByWithdrawRequestId(String withdrawRequestId) {
        var withdraw = withdrawRepository.findById(withdrawRequestId).orElseThrow(() -> new ValidationException("Withdraw request not found"));
        if (withdraw.getQrCodeUrl() == null) {
            throw new ValidationException("QR code not found for withdraw request ID: " + withdrawRequestId);
        }
        if (withdraw.getStatus() != WithdrawStatusEnum.PENDING) {
            throw new ValidationException("QR code not found for withdraw request ID: " + withdrawRequestId + " because status is " + withdraw.getStatus());
        }
        return HashUtils.decodeDataUrl(withdraw.getQrCodeUrl());
    }

    private WithdrawRequestBaseResponse wrapWithdrawRequestBaseResponse(WithdrawRequestEntity withdrawRequestEntity, Map<String, VietQrBankEntity> bankMap) {
        var bank = bankMap.get(withdrawRequestEntity.getBankBin());
        return WithdrawRequestBaseResponse.builder()
                .id(withdrawRequestEntity.getId())
                .amount(withdrawRequestEntity.getAmount())
                .status(withdrawRequestEntity.getStatus())
                .bankBin(withdrawRequestEntity.getBankBin())
                .bankName(bank.getName())
                .bankUrl(bank.getLogo())
                .build();
    }

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<WithdrawRequestEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = withdrawRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
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
