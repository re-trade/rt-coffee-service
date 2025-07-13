package org.retrade.main.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.IdentityVerifiedStatusEnum;
import org.retrade.main.model.dto.request.ApproveSellerRequest;
import org.retrade.main.model.dto.request.SellerRegisterRequest;
import org.retrade.main.model.dto.request.SellerUpdateRequest;
import org.retrade.main.model.dto.response.SellerBaseResponse;
import org.retrade.main.model.dto.response.SellerRegisterResponse;
import org.retrade.main.model.entity.AccountRoleEntity;
import org.retrade.main.model.entity.RoleEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.retrade.main.model.message.CCCDVerificationMessage;
import org.retrade.main.model.message.CCCDVerificationResultMessage;
import org.retrade.main.model.other.SellerWrapperBase;
import org.retrade.main.repository.AccountRoleRepository;
import org.retrade.main.repository.RoleRepository;
import org.retrade.main.repository.SellerRepository;
import org.retrade.main.service.MessageProducerService;
import org.retrade.main.service.SellerService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {
    private final SellerRepository sellerRepository;
    private final MessageProducerService messageProducerService;
    private final AuthUtils authUtils;
    private final RoleRepository roleRepository;
    private final AccountRoleRepository accountRoleRepository;

    @Override
    public SellerRegisterResponse createSeller(SellerRegisterRequest request) {
        if (sellerRepository.existsByIdentityNumberIgnoreCase(request.getIdentityNumber())) {
            throw new ValidationException("This identity is existed");
        }
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
                .balance(BigDecimal.ZERO)
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
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public void approveSeller(ApproveSellerRequest request) {
        var roles = authUtils.getRolesFromAuthUser();
        if (!roles.contains("ROLE_ADMIN")) {
            throw new ValidationException("User does not have permission to approve seller");
        }
        var sellerEntity = sellerRepository.findById(request.getSellerId()).orElseThrow(() -> new ValidationException("No such seller existed seller"));
        validateApproveSeller(sellerEntity, request.getForced());
        if (!request.getApprove()) {
            sellerEntity.setVerified(false);
            sellerEntity.setIdentityVerified(IdentityVerifiedStatusEnum.FAILED);
            sellerEntity.setFrontSideIdentityCard("example");
            sellerEntity.setBackSideIdentityCard("example");
        } else {
            sellerEntity.setVerified(true);
            var roleEntity = roleRepository.findByCode("ROLE_SELLER").orElseThrow(() -> new ValidationException("System can't sign role as this moment, please try again next time"));
            signRoleSellerToUser(sellerEntity, roleEntity);
        }
        try {
            sellerRepository.save(sellerEntity);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to approve seller", ex);
        }
    }

    @Override
    public PaginationWrapper<List<SellerBaseResponse>> getSellers(QueryWrapper wrapper) {
        return sellerRepository.query(wrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            return getSellerPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::wrapSellerBaseResponse).stream().toList();
            return new PaginationWrapper.Builder<List<SellerBaseResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
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
        var sellerEntity = accountEntity.getSeller();
        if (sellerEntity == null) {
            throw new ValidationException("Account is not a seller");
        }
        sellerEntity.setAvatarUrl(request.getAvatarUrl());
        sellerEntity.setBackground(request.getBackground());
        sellerEntity.setShopName(request.getShopName());
        sellerEntity.setDescription(request.getDescription());
        sellerEntity.setAddressLine(request.getAddressLine());
        sellerEntity.setDistrict(request.getDistrict());
        sellerEntity.setWard(request.getWard());
        sellerEntity.setState(request.getState());
        sellerEntity.setEmail(request.getEmail());
        sellerEntity.setPhoneNumber(request.getPhoneNumber());
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

    @Override
    public SellerBaseResponse getSellerDetails(String id) {
        SellerEntity  seller = sellerRepository.findById(id).orElseThrow(
                () -> new ValidationException("No such seller existed seller")
        );
        if(!seller.getVerified()){
            throw new ValidationException("Seller is not verified");
        }
        return wrapSellerBaseResponse(seller);
    }

    @Override
    public SellerBaseResponse getMySellers() {
        var accountEntity = authUtils.getUserAccountFromAuthentication();
        if (accountEntity.getSeller() == null) {
            throw new ValidationException("Seller is not exist");
        }
        return wrapSellerBaseResponse(accountEntity.getSeller());
    }

    @Override
    public SellerBaseResponse banSeller(String sellerId) {
        var roles = authUtils.getRolesFromAuthUser();
        if (!roles.contains("ROLE_ADMIN")) {
            throw new ValidationException("User does not have permission to approve seller");
        }
        SellerEntity seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ValidationException("Seller not found with ID: " + sellerId));
        seller.setVerified(false);
        sellerRepository.save(seller);
        return wrapSellerBaseResponse(seller);
    }

    @Override
    public SellerBaseResponse unbanSeller(String sellerId) {
        var roles = authUtils.getRolesFromAuthUser();
        if (!roles.contains("ROLE_ADMIN")) {
            throw new ValidationException("User does not have permission to approve seller");
        }
        SellerEntity seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ValidationException("Seller not found with ID: " + sellerId));
        seller.setVerified(true);
        sellerRepository.save(seller);
        return wrapSellerBaseResponse(seller);
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
                .addressLine(sellerEntity.getAddressLine())
                .district(sellerEntity.getDistrict())
                .ward(sellerEntity.getWard())
                .state(sellerEntity.getState())
                .build();
    }

    private void signRoleSellerToUser(SellerEntity sellerEntity, RoleEntity roleEntity) {
        var account = sellerEntity.getAccount();
        var accountRole = AccountRoleEntity.builder()
                .account(account)
                .role(roleEntity)
                .enabled(true)
                .build();
        try {
            accountRoleRepository.save(accountRole);
        }catch (Exception ex) {
            throw new ActionFailedException("Failed to sign seller role to user", ex);
        }
    }

    private void validateApproveSeller(SellerEntity sellerEntity, boolean force) {
        if (force) {
            return;
        }
        if (sellerEntity.getVerified()) {
            throw new ValidationException("Seller already verified");
        }
        if (sellerEntity.getBackSideIdentityCard().equals("example") || sellerEntity.getFrontSideIdentityCard().equals("example")) {
            throw new ValidationException("Seller has not uploaded identity card");
        }
        if (sellerEntity.getIdentityVerified() != IdentityVerifiedStatusEnum.WAITING) {
            throw new ValidationException("Seller identity verification status is not waiting");
        }
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

    private Predicate getSellerPredicate(Map<String, QueryFieldWrapper> param, Root<SellerEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = sellerRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

}
