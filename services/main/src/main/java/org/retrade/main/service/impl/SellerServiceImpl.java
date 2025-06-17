package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.IdentityVerifiedStatusEnum;
import org.retrade.main.model.dto.request.SellerRegisterRequest;
import org.retrade.main.model.dto.request.SellerUpdateRequest;
import org.retrade.main.model.dto.response.SellerBaseResponse;
import org.retrade.main.model.dto.response.SellerRegisterResponse;
import org.retrade.main.model.entity.SellerEntity;
import org.retrade.main.model.message.CCCDVerificationMessage;
import org.retrade.main.model.message.CCCDVerificationResultMessage;
import org.retrade.main.model.other.SellerWrapperBase;
import org.retrade.main.repository.SellerRepository;
import org.retrade.main.service.MessageProducerService;
import org.retrade.main.service.SellerService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
                .addressLine(request.getAddressLine())
                .district(request.getDistrict())
                .ward(request.getWard())
                .state(request.getState())
                .email(request.getEmail())
                .avatarUrl(request.getAvatarUrl())
                .background(request.getBackground())
                .phoneNumber(request.getPhoneNumber())
                .identityNumber(request.getIdentityNumber())
                .frontSideIdentityCard("example")
                .backSideIdentityCard("example")
                .identityVerified(IdentityVerifiedStatusEnum.INIT)
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
        if (sellerEntity.getIdentityVerified() == IdentityVerifiedStatusEnum.VERIFIED) {
            throw new ValidationException("Seller already verified");
        }
        sellerEntity.setIdentityVerified(IdentityVerifiedStatusEnum.WAITING);
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
                .addressLine(request.getAddressLine())
                .district(request.getDistrict())
                .ward(request.getWard())
                .state(request.getState())
                .email(request.getEmail())
                .avatarUrl(request.getAvatarUrl())
                .background(request.getBackground())
                .phoneNumber(request.getPhoneNumber())
                .verified(false)
                .identityVerified(IdentityVerifiedStatusEnum.VERIFIED)
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
    public void updateVerifiedSeller(CCCDVerificationResultMessage message) {
        var seller = sellerRepository.findById(message.getSellerId()).orElseThrow(() -> new ValidationException("No such seller existed seller"));
        if (message.getAccepted()) {
            seller.setIdentityVerified(IdentityVerifiedStatusEnum.VERIFIED);
        }
        try {
            sellerRepository.save(seller);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to update", ex);
        }
    }

    @Override
    public Optional<SellerWrapperBase> getSellerBaseInfoById(String sellerId) {
        var sellerResult = sellerRepository.findById(sellerId);
        if (sellerResult.isEmpty()) {
            return Optional.empty();
        }
        var account = sellerResult.get().getAccount();
        return Optional.of(new SellerWrapperBase(sellerId, account.getEmail(), account.getUsername()));
    }

    private SellerBaseResponse wrapSellerBaseResponse(SellerEntity sellerEntity) {
        return SellerBaseResponse.builder()
                .id(sellerEntity.getId())
                .shopName(sellerEntity.getShopName())
                .description(sellerEntity.getDescription())

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
                .addressLine(sellerEntity.getAddressLine())
                .district(sellerEntity.getDistrict())
                .ward(sellerEntity.getWard())
                .state(sellerEntity.getState())
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
