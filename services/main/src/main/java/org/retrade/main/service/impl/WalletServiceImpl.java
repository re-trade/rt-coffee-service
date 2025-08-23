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
import org.retrade.main.config.common.WithdrawConfig;
import org.retrade.main.model.constant.WithdrawStatusEnum;
import org.retrade.main.model.dto.request.VietQrGenerateRequest;
import org.retrade.main.model.dto.request.WithdrawApproveRequest;
import org.retrade.main.model.dto.request.WithdrawRequest;
import org.retrade.main.model.dto.response.*;
import org.retrade.main.model.entity.*;
import org.retrade.main.model.message.SocketNotificationMessage;
import org.retrade.main.repository.jpa.*;
import org.retrade.main.repository.redis.VietQrBankRepository;
import org.retrade.main.service.MessageProducerService;
import org.retrade.main.service.VietQRService;
import org.retrade.main.service.WalletService;
import org.retrade.main.util.AuthUtils;
import org.retrade.main.util.HashUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final AuthUtils authUtils;
    private final AccountRepository accountRepository;
    private final CustomerBankInfoRepository customerBankInfoRepository;
    private final TransactionRepository transactionRepository;
    private final WithdrawRepository withdrawRepository;
    private final VietQrBankRepository vietQrBankRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WithdrawConfig withdrawConfig;
    private final MessageProducerService messageProducerService;
    private final VietQRService vietQRService;


    @Override
    public AccountWalletResponse getUserAccountWallet() {
        var account = authUtils.getUserAccountFromAuthentication();
        return wrapAccountWalletResponse(account);
    }

    @Override
    public void withdrawRequest(WithdrawRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        var bankInfo = validateWithdrawRequest(request, account);
        var withdraw = WithdrawRequestEntity.builder()
                .account(account)
                .amount(request.getAmount())
                .status(WithdrawStatusEnum.PENDING)
                .bankAccount(bankInfo.getAccountNumber())
                .bankBin(bankInfo.getBankBin())
                .notes(request.getContent())
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

    @Override
    @Transactional(rollbackFor = {ValidationException.class, ActionFailedException.class})
    public void approveWithdrawRequest(WithdrawApproveRequest request) {
        var withdraw = withdrawRepository.findById(request.getWithdrawId()).orElseThrow(() -> new ValidationException("Withdraw request not found"));
        var account = withdraw.getAccount();
        if (withdraw.getStatus() != WithdrawStatusEnum.PENDING) {
            throw new ValidationException("Withdraw status is not PENDING");
        }
        if (request.getApproved()) {
            if (account.getBalance().compareTo(withdraw.getAmount()) < 0) {
                throw new ValidationException("Insufficient balance");
            }
            withdraw.setStatus(WithdrawStatusEnum.COMPLETED);
            withdraw.setProcessedDate(new Timestamp(System.currentTimeMillis()));
            updateBalance(account.getBalance().subtract(withdraw.getAmount()), account);
            var transaction = WalletTransactionEntity.builder()
                    .account(account)
                    .amount(withdraw.getAmount().negate())
                    .note("Withdraw request approved")
                    .build();
            walletTransactionRepository.save(transaction);

        } else {
            withdraw.setStatus(WithdrawStatusEnum.REJECTED);
            withdraw.setProcessedDate(new Timestamp(System.currentTimeMillis()));
            withdraw.setCancelReason(request.getRejectReason());
        }
        try {
            withdrawRepository.save(withdraw);
        } catch (Exception ex) {
            throw new ActionFailedException("Have a problem when approve withdraw request", ex);
        }

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
    public PaginationWrapper<List<WithdrawRequestBaseResponse>> getAccountWithdrawRequest(QueryWrapper queryWrapper) {
        var account = authUtils.getUserAccountFromAuthentication();
        return withdrawRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("account"), account));
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
        var processedDate = withdrawRequestEntity.getProcessedDate() != null ? withdrawRequestEntity.getProcessedDate().toLocalDateTime() : null;
        return WithdrawRequestBaseResponse.builder()
                .id(withdrawRequestEntity.getId())
                .amount(withdrawRequestEntity.getAmount())
                .status(withdrawRequestEntity.getStatus())
                .bankBin(withdrawRequestEntity.getBankBin())
                .bankName(bank.getName())
                .bankUrl(bank.getLogo())
                .processedDate(processedDate)
                .createdDate(withdrawRequestEntity.getCreatedDate().toLocalDateTime())
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

    @Override
    public WithdrawRequestDetailResponse getWithdrawRequestDetail(String withdrawRequestId) {
        var withdrawRequestEntity = withdrawRepository.findById(withdrawRequestId).orElseThrow(() -> new ValidationException("Withdraw request not found"));
        return wrapWithdrawRequestDetailResponse(withdrawRequestEntity);
    }

    private void sendSocketNotification(AccountEntity account) {
        messageProducerService.sendSocketNotification(SocketNotificationMessage.builder()
                        .messageId(UUID.randomUUID().toString())
                        .accountId(account.getId())
                        .title("Withdraw request")
                        .type("")
                        .message("Your withdraw request has been submitted. Please check your wallet for the status of your withdraw request.")
                        .content("")
                .build());
    }

    private WithdrawRequestDetailResponse wrapWithdrawRequestDetailResponse(WithdrawRequestEntity withdrawRequestEntity) {
        var map = vietQrBankRepository.getBankMap();
        var bank = map.get(withdrawRequestEntity.getBankBin());
        var account = withdrawRequestEntity.getAccount();
        var customer = account.getCustomer();
        var seller = account.getSeller();
        var withdrawBuilder = WithdrawRequestDetailResponse.builder()
                .id(withdrawRequestEntity.getId())
                .amount(withdrawRequestEntity.getAmount())
                .status(withdrawRequestEntity.getStatus())
                .username(account.getUsername())
                .bankName(bank.getName())
                .cancelReason(withdrawRequestEntity.getCancelReason())
                .bankUrl(bank.getLogo())
                .bankBin(withdrawRequestEntity.getBankBin());
        if (customer != null) {
            withdrawBuilder.customerName(customer.getFirstName() + " " + customer.getLastName());
            withdrawBuilder.customerEmail(customer.getAccount().getEmail());
            withdrawBuilder.customerPhone(customer.getPhone());
            withdrawBuilder.customerAvatarUrl(customer.getAvatarUrl());
        }
        if (seller != null) {
            withdrawBuilder.sellerAvatarUrl(seller.getAvatarUrl());
            withdrawBuilder.sellerName(seller.getShopName());
            withdrawBuilder.sellerPhone(seller.getPhoneNumber());
            withdrawBuilder.sellerEmail(seller.getEmail());
        }
        return withdrawBuilder.build();
    }

    public AccountWalletResponse wrapAccountWalletResponse(AccountEntity accountEntity) {
        return AccountWalletResponse.builder()
                    .accountId(accountEntity.getId())
                    .balance(accountEntity.getBalance())
                    .build();
    }

    private CustomerBankInfoEntity validateWithdrawRequest(WithdrawRequest request, AccountEntity account) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        if (request.getAmount().compareTo(account.getBalance()) > 0) {
            throw new ValidationException("Số dư không đủ");
        }
        if (request.getAmount().compareTo(withdrawConfig.getMinWithdraw()) < 0) {
            throw new ValidationException("Số tiền rút tối thiểu là " + withdrawConfig.getMinWithdraw());
        }
        if (request.getAmount().compareTo(withdrawConfig.getMaxWithdraw()) > 0) {
            throw new ValidationException("Số tiền rút tối đa mỗi lần là " + withdrawConfig.getMaxWithdraw());
        }
        var bankInfo = customerBankInfoRepository.findById(request.getBankProfileId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy tài khoản ngân hàng"));
        BigDecimal todayTotal = withdrawRepository.sumAmountByAccountAndDate(account.getId(), startOfDay, endOfDay, WithdrawStatusEnum.REJECTED);
        if (todayTotal.add(request.getAmount()).compareTo(withdrawConfig.getDailyLimit()) > 0) {
            throw new ValidationException("Vượt quá hạn mức rút tiền trong ngày");
        }
        long pendingCount = withdrawRepository.countByAccountAndStatus(account.getId(), WithdrawStatusEnum.PENDING);
        if (pendingCount >= withdrawConfig.getMaxPendingRequest()) {
            throw new ValidationException("Bạn đã có quá nhiều yêu cầu rút tiền đang chờ xử lý");
        }
        if (request.getContent() != null && request.getContent().length() > 255) {
            throw new ValidationException("Nội dung rút tiền quá dài");
        }
        return bankInfo;
    }
}
