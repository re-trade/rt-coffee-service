package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.SellerRegisterRequest;
import org.retrade.main.model.dto.request.SellerUpdateRequest;
import org.retrade.main.model.dto.response.SellerBaseResponse;
import org.retrade.main.model.dto.response.SellerRegisterResponse;
import org.retrade.main.model.entity.SellerEntity;
import org.retrade.main.model.message.CCCDVerificationMessage;
import org.retrade.main.model.message.CCCDVerificationResultMessage;
import org.retrade.main.repository.SellerRepository;
import org.retrade.main.service.MessageProducerService;
import org.retrade.main.service.SellerService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {
    private final SellerRepository sellerRepository;
    private final MessageProducerService messageProducerService;
    private final AuthUtils authUtils;

    @Override
    public SellerRegisterResponse createSeller(SellerRegisterRequest request) {
        var accountEntity = authUtils.getUserAccountFromAuthentication();
        if (accountEntity.getCustomer() == null) {
            throw new ValidationException("Account must be a customer to create a seller");
        }
        if (accountEntity.getSeller() != null) {
            throw new ValidationException("Account already has a seller");
        }
        var sellerEntity = SellerEntity.builder()
                .shopName(request.getShopName())
                .description(request.getDescription())
                .address(request.getAddress())
                .businessType(request.getBusinessType())
                .taxCode(request.getTaxCode())
                .email(request.getEmail())
                .avatarUrl(request.getAvatarUrl())
                .background(request.getBackground())
                .phoneNumber(request.getPhoneNumber())
                .identityNumber(request.getIdentityNumber())
                .frontSideIdentityCard("example")
                .backSideIdentityCard("example")
                .verified(false)
                .account(accountEntity)
                .build();
        try {
            var result = sellerRepository.save(sellerEntity);
            return wrapSellerRegisterResponse(result);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to create seller", ex);
        }
    }

    @Override
    public SellerRegisterResponse cccdSubmit(String front, String back) {
        var sellerEntity = getSellerEntity(front, back);
        try {
            var result = sellerRepository.save(sellerEntity);
            var messageWrapper = CCCDVerificationMessage.builder()
                    .sellerId(result.getId())
                    .identityNumber(result.getIdentityNumber())
                    .backUrl(sellerEntity.getBackSideIdentityCard())
                    .frontUrl(sellerEntity.getFrontSideIdentityCard())
                    .build();
            messageProducerService.sendCCCDForVerified(messageWrapper);
            return wrapSellerRegisterResponse(result);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to create seller", ex);
        }
    }

    @NotNull
    private SellerEntity getSellerEntity(String front, String back) {
        var accountEntity = authUtils.getUserAccountFromAuthentication();
        if (accountEntity.getCustomer() == null) {
            throw new ValidationException("Account must be a customer to create a seller");
        }
        if (accountEntity.getSeller() == null) {
            throw new ValidationException("Seller is not exist");
        }
        var sellerEntity = accountEntity.getSeller();
        sellerEntity.setFrontSideIdentityCard(front);
        sellerEntity.setBackSideIdentityCard(back);
        return sellerEntity;
    }

    @Override
    public SellerBaseResponse updateSellerProfile(SellerUpdateRequest request) {
        var accountEntity = authUtils.getUserAccountFromAuthentication();
        if (accountEntity.getCustomer() == null) {
            throw new ValidationException("Account must be a customer to create a seller");
        }
        if (accountEntity.getSeller() != null) {
            throw new ValidationException("Account already has a seller");
        }
        var sellerEntity = SellerEntity.builder()
                .shopName(request.getShopName())
                .description(request.getDescription())
                .address(request.getAddress())
                .email(request.getEmail())
                .avatarUrl(request.getAvatarUrl())
                .background(request.getBackground())
                .phoneNumber(request.getPhoneNumber())
                .verified(false)
                .account(accountEntity)
                .build();
        try {
            var result = sellerRepository.save(sellerEntity);
            return wrapSellerBaseResponse(result);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to create seller", ex);
        }
    }

    @Override
    public void updateVerifiedSeller(CCCDVerificationResultMessage message) {}

    private SellerBaseResponse wrapSellerBaseResponse(SellerEntity sellerEntity) {
        return SellerBaseResponse.builder()
                .id(sellerEntity.getId())
                .shopName(sellerEntity.getShopName())
                .description(sellerEntity.getDescription())
                .businessType(sellerEntity.getBusinessType())
                .address(sellerEntity.getAddress())
                .avatarUrl(sellerEntity.getAvatarUrl())
                .email(sellerEntity.getEmail())
                .background(sellerEntity.getBackground())
                .phoneNumber(sellerEntity.getPhoneNumber())
                .verified(sellerEntity.getVerified())
                .createdAt(sellerEntity.getCreatedDate().toLocalDateTime())
                .updatedAt(sellerEntity.getUpdatedDate().toLocalDateTime())
                .build();
    }


    private SellerRegisterResponse wrapSellerRegisterResponse(SellerEntity sellerEntity) {
        return SellerRegisterResponse.builder()
                .id(sellerEntity.getId())
                .shopName(sellerEntity.getShopName())
                .description(sellerEntity.getDescription())
                .businessType(sellerEntity.getBusinessType())
                .address(sellerEntity.getAddress())
                .avatarUrl(sellerEntity.getAvatarUrl())
                .email(sellerEntity.getEmail())
                .background(sellerEntity.getBackground())
                .phoneNumber(sellerEntity.getPhoneNumber())
                .verified(sellerEntity.getVerified())
                .createdAt(sellerEntity.getCreatedDate().toLocalDateTime())
                .updatedAt(sellerEntity.getUpdatedDate().toLocalDateTime())
                .build();
    }

}
