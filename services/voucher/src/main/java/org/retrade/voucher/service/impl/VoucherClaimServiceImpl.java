package org.retrade.voucher.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.voucher.model.constant.VoucherStatusEnum;
import org.retrade.voucher.model.dto.request.ClaimVoucherRequest;
import org.retrade.voucher.model.dto.response.VoucherClaimResponse;
import org.retrade.voucher.model.dto.response.VoucherClaimSimpleResponse;
import org.retrade.voucher.model.entity.VoucherEntity;
import org.retrade.voucher.model.entity.VoucherUsageEntity;
import org.retrade.voucher.model.entity.VoucherVaultEntity;
import org.retrade.voucher.repository.VoucherUsageRepository;
import org.retrade.voucher.repository.VoucherVaultRepository;
import org.retrade.voucher.service.VoucherClaimService;
import org.retrade.voucher.service.VoucherService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherClaimServiceImpl implements VoucherClaimService {
    private final VoucherService voucherService;
    private final VoucherVaultRepository voucherVaultRepository;
    private final VoucherUsageRepository voucherUsageRepository;

    @Override
    @Transactional
    public VoucherClaimResponse claimVoucher(ClaimVoucherRequest request) {
        VoucherEntity voucher = voucherService.getVoucherEntityByCode(request.getCode());
        if (!voucher.getActivated()) {
            throw new ValidationException("Voucher is not active");
        }
        if (voucher.getExpiredDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Voucher has expired");
        }
        if (voucher.getMaxUses() != null && voucher.getMaxUses() > 0) {
            throw new ValidationException("Voucher has reached maximum uses");
        }
        voucherVaultRepository.findByAccountIdAndVoucher(request.getAccountId(), voucher)
                .ifPresent(existingVault -> {
                    throw new ValidationException("You have already claimed this voucher");
                });
        VoucherVaultEntity voucherVault = new VoucherVaultEntity();
        voucherVault.setAccountId(request.getAccountId());
        voucherVault.setVoucher(voucher);
        voucherVault.setStatus(VoucherStatusEnum.ACTIVE.name());
        voucherVault.setClaimedDate(Timestamp.valueOf(LocalDateTime.now()));
        voucherVault.setExpiredDate(Timestamp.valueOf(voucher.getExpiredDate()));
        VoucherVaultEntity savedVault = voucherVaultRepository.save(voucherVault);
        
        return mapToVoucherClaimResponse(savedVault);
    }

    @Override
    public List<VoucherClaimResponse> getUserVouchers(String accountId) {
        List<VoucherVaultEntity> userVouchers = voucherVaultRepository.findByAccountId(accountId);
        return userVouchers.stream()
                .map(this::mapToVoucherClaimResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherClaimResponse> getUserActiveVouchers(String accountId) {
        List<VoucherVaultEntity> activeVouchers = voucherVaultRepository.findByAccountIdAndStatus(
                accountId, VoucherStatusEnum.ACTIVE.name());
        return activeVouchers.stream()
                .map(this::mapToVoucherClaimResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markVoucherAsUsed(String vaultId, String orderId) {
        VoucherVaultEntity voucherVault = voucherVaultRepository.findById(vaultId)
                .orElseThrow(() -> new ValidationException("Voucher vault not found with id: " + vaultId));

        if (!voucherVault.getStatus().equals(VoucherStatusEnum.ACTIVE.name())) {
            throw new ValidationException("Voucher is not active");
        }
        voucherVault.setStatus(VoucherStatusEnum.USED.name());
        voucherVault.setUsedDate(Timestamp.valueOf(LocalDateTime.now()));
        voucherVaultRepository.save(voucherVault);
        VoucherEntity voucher = voucherVault.getVoucher();

        VoucherUsageEntity voucherUsage = new VoucherUsageEntity();
        voucherUsage.setVoucher(voucher);
        voucherUsage.setVoucherVault(voucherVault);
        voucherUsage.setOrderId(orderId);
        voucherUsage.setUserId(voucherVault.getAccountId());
        voucherUsage.setUsageDate(LocalDateTime.now());
        voucherUsage.setDiscountApplied(calculateDiscount(voucher));
        voucherUsage.setType(voucher.getType());
        
        voucherUsageRepository.save(voucherUsage);
    }
    
    private BigDecimal calculateDiscount(VoucherEntity voucher) {
        return BigDecimal.valueOf(voucher.getDiscount());
    }
    
    @Override
    public VoucherClaimSimpleResponse claimVoucherSimple(ClaimVoucherRequest request) {
        VoucherEntity voucher = voucherService.getVoucherEntityByCode(request.getCode());
        if (!voucher.getActivated()) {
            throw new ValidationException("Voucher is not active");
        }
        if (voucher.getExpiredDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Voucher has expired");
        }
        if (voucher.getMaxUses() != null && voucher.getMaxUses() > 0) {
            throw new ValidationException("Voucher has reached maximum uses");
        }
        voucherVaultRepository.findByAccountIdAndVoucher(request.getAccountId(), voucher)
                .ifPresent(existingVault -> {
                    throw new ValidationException("You have already claimed this voucher");
                });
        VoucherVaultEntity voucherVault = new VoucherVaultEntity();
        voucherVault.setAccountId(request.getAccountId());
        voucherVault.setVoucher(voucher);
        voucherVault.setStatus(VoucherStatusEnum.ACTIVE.name());
        voucherVault.setClaimedDate(Timestamp.valueOf(LocalDateTime.now()));
        voucherVault.setExpiredDate(Timestamp.valueOf(voucher.getExpiredDate()));
        VoucherVaultEntity savedVault = voucherVaultRepository.save(voucherVault);

        return mapToVoucherClaimSimpleResponse(savedVault);
    }

    @Override
    public List<VoucherClaimSimpleResponse> getUserVouchersSimple(String accountId) {
        List<VoucherVaultEntity> userVouchers = voucherVaultRepository.findByAccountId(accountId);
        return userVouchers.stream()
                .map(this::mapToVoucherClaimSimpleResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherClaimSimpleResponse> getUserActiveVouchersSimple(String accountId) {
        List<VoucherVaultEntity> activeVouchers = voucherVaultRepository.findByAccountIdAndStatus(
                accountId, VoucherStatusEnum.ACTIVE.name());
        return activeVouchers.stream()
                .map(this::mapToVoucherClaimSimpleResponse)
                .collect(Collectors.toList());
    }

    private VoucherClaimResponse mapToVoucherClaimResponse(VoucherVaultEntity entity) {
        VoucherEntity voucher = entity.getVoucher();
        return VoucherClaimResponse.builder()
                .id(entity.getId())
                .voucherId(voucher.getId())
                .code(voucher.getCode())
                .type(voucher.getType())
                .discount(voucher.getDiscount())
                .expiryDate(voucher.getExpiredDate())
                .status(entity.getStatus())
                .build();
    }

    private VoucherClaimSimpleResponse mapToVoucherClaimSimpleResponse(VoucherVaultEntity entity) {
        VoucherEntity voucher = entity.getVoucher();
        return VoucherClaimSimpleResponse.builder()
                .id(entity.getId())
                .code(voucher.getCode())
                .type(voucher.getType())
                .discount(voucher.getDiscount())
                .expiryDate(voucher.getExpiredDate())
                .minSpend(voucher.getMinSpend())
                .status(entity.getStatus())
                .title(voucher.getName())
                .description(voucher.getDescription())
                .build();
    }
}
