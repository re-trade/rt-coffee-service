package org.retrade.voucher.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.voucher.model.constant.VoucherStatusEnum;
import org.retrade.voucher.model.constant.VoucherTypeEnum;
import org.retrade.voucher.model.dto.request.ApplyVoucherRequest;
import org.retrade.voucher.model.dto.request.ValidateVoucherRequest;
import org.retrade.voucher.model.dto.response.VoucherValidationResponse;
import org.retrade.voucher.model.entity.VoucherEntity;
import org.retrade.voucher.model.entity.VoucherRestrictionEntity;
import org.retrade.voucher.model.entity.VoucherVaultEntity;
import org.retrade.voucher.repository.VoucherRestrictionRepository;
import org.retrade.voucher.repository.VoucherUsageRepository;
import org.retrade.voucher.repository.VoucherVaultRepository;
import org.retrade.voucher.service.VoucherClaimService;
import org.retrade.voucher.service.VoucherService;
import org.retrade.voucher.service.VoucherValidationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherValidationServiceImpl implements VoucherValidationService {
    private final VoucherService voucherService;
    private final VoucherClaimService voucherClaimService;
    private final VoucherVaultRepository voucherVaultRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final VoucherRestrictionRepository voucherRestrictionRepository;

    @Override
    public VoucherValidationResponse validateVoucher(ValidateVoucherRequest request) {
        try {
            // Get voucher by code
            VoucherEntity voucher = voucherService.getVoucherEntityByCode(request.getCode());
            
            // Check if voucher is active
            if (!voucher.getActived()) {
                return createInvalidResponse("Voucher is not active", voucher);
            }
            
            // Check if voucher has expired
            if (voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
                return createInvalidResponse("Voucher has expired", voucher);
            }
            
            // Check if voucher has reached max uses
            if (voucher.getMaxUses() != null && voucher.getMaxUses() > 0 && voucher.getCurrentUses() >= voucher.getMaxUses()) {
                return createInvalidResponse("Voucher has reached maximum uses", voucher);
            }
            
            // Check if user has claimed this voucher
            Optional<VoucherVaultEntity> voucherVaultOpt = voucherVaultRepository.findByAccountIdAndVoucher(
                    request.getAccountId(), voucher);
            
            if (voucherVaultOpt.isEmpty()) {
                return createInvalidResponse("You have not claimed this voucher", voucher);
            }
            
            VoucherVaultEntity voucherVault = voucherVaultOpt.get();
            
            // Check if voucher is active for this user
            if (!voucherVault.getStatus().equals(VoucherStatusEnum.ACTIVE.name())) {
                return createInvalidResponse("Voucher is not active for this user", voucher);
            }
            
            // Check if user has reached max uses per user
            int userUsageCount = voucherUsageRepository.countByVoucherAndUserId(voucher, request.getAccountId());
            if (voucher.getMaxUsesPerUser() != null && voucher.getMaxUsesPerUser() > 0 && 
                    userUsageCount >= voucher.getMaxUsesPerUser()) {
                return createInvalidResponse("You have reached maximum uses for this voucher", voucher);
            }
            
            // Check minimum spend
            if (voucher.getMinSpend() != null && voucher.getMinSpend() > 0 && 
                    request.getOrderTotal().compareTo(BigDecimal.valueOf(voucher.getMinSpend())) < 0) {
                return createInvalidResponse(
                        "Order total does not meet minimum spend requirement of " + voucher.getMinSpend(), voucher);
            }
            
            // Check product restrictions
            List<VoucherRestrictionEntity> restrictions = voucherRestrictionRepository.findByVoucher(voucher);
            if (!restrictions.isEmpty()) {
                List<String> restrictedProductIds = restrictions.stream()
                        .map(VoucherRestrictionEntity::getProductId)
                        .collect(Collectors.toList());
                
                // If there are product restrictions, check if any of the products in the order match
                if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
                    boolean hasMatchingProduct = request.getProductIds().stream()
                            .anyMatch(restrictedProductIds::contains);
                    
                    if (!hasMatchingProduct) {
                        return createInvalidResponse("Voucher is not applicable to any products in your order", voucher);
                    }
                } else {
                    return createInvalidResponse("Product information is required for this voucher", voucher);
                }
            }
            
            // Calculate discount amount
            BigDecimal discountAmount = calculateDiscountAmount(voucher, request.getOrderTotal());
            
            // Return valid response
            return VoucherValidationResponse.builder()
                    .valid(true)
                    .message("Voucher is valid")
                    .voucherId(voucher.getId())
                    .code(voucher.getCode())
                    .discountAmount(discountAmount)
                    .type(voucher.getType())
                    .build();
            
        } catch (ValidationException e) {
            return VoucherValidationResponse.builder()
                    .valid(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public VoucherValidationResponse applyVoucher(ApplyVoucherRequest request) {
        try {
            // Get voucher by code
            VoucherEntity voucher = voucherService.getVoucherEntityByCode(request.getCode());
            
            // Validate voucher (simplified validation for brevity)
            ValidateVoucherRequest validateRequest = ValidateVoucherRequest.builder()
                    .code(request.getCode())
                    .accountId(request.getAccountId())
                    .orderTotal(request.getOrderTotal())
                    .build();
            
            VoucherValidationResponse validationResponse = validateVoucher(validateRequest);
            
            if (!validationResponse.isValid()) {
                return validationResponse;
            }
            
            // Get voucher vault
            Optional<VoucherVaultEntity> voucherVaultOpt = voucherVaultRepository.findByAccountIdAndVoucher(
                    request.getAccountId(), voucher);
            
            if (voucherVaultOpt.isEmpty()) {
                return VoucherValidationResponse.builder()
                        .valid(false)
                        .message("You have not claimed this voucher")
                        .build();
            }
            
            // Mark voucher as used
            voucherClaimService.markVoucherAsUsed(voucherVaultOpt.get().getId(), request.getOrderId());
            
            // Calculate discount amount
            BigDecimal discountAmount = calculateDiscountAmount(voucher, request.getOrderTotal());
            
            // Return success response
            return VoucherValidationResponse.builder()
                    .valid(true)
                    .message("Voucher applied successfully")
                    .voucherId(voucher.getId())
                    .code(voucher.getCode())
                    .discountAmount(discountAmount)
                    .type(voucher.getType())
                    .build();
            
        } catch (Exception e) {
            return VoucherValidationResponse.builder()
                    .valid(false)
                    .message("Failed to apply voucher: " + e.getMessage())
                    .build();
        }
    }
    
    private VoucherValidationResponse createInvalidResponse(String message, VoucherEntity voucher) {
        return VoucherValidationResponse.builder()
                .valid(false)
                .message(message)
                .voucherId(voucher.getId())
                .code(voucher.getCode())
                .build();
    }
    
    private BigDecimal calculateDiscountAmount(VoucherEntity voucher, BigDecimal orderTotal) {
        if (voucher.getType().equals(VoucherTypeEnum.PERCENTAGE.name())) {
            // Calculate percentage discount
            return orderTotal.multiply(BigDecimal.valueOf(voucher.getDiscount() / 100.0));
        } else {
            // Fixed amount discount
            BigDecimal fixedDiscount = BigDecimal.valueOf(voucher.getDiscount());
            // Ensure discount doesn't exceed order total
            return fixedDiscount.min(orderTotal);
        }
    }
}
