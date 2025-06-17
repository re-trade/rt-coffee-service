package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.IdentityVerifiedStatusEnum;
import org.retrade.main.model.entity.AccountRoleEntity;
import org.retrade.main.repository.*;
import org.retrade.main.service.SystemService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {
    private final SellerRepository sellerRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final AuthUtils authUtils;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void approveSeller(String sellerId) {
        var sellerEntity = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ValidationException("Seller not found with ID: " + sellerId));

        if (sellerEntity.getVerified()) {
            throw new ValidationException("Seller is already verified");
        }

        sellerEntity.setVerified(true);

        var role = roleRepository.findByCode("ROLE_SELLER")
                .orElseThrow(() -> new ValidationException("Role not found"));
        var accountRoleEntity = AccountRoleEntity.builder()
                .account(sellerEntity.getAccount())
                .role(role)
                .enabled(true)
                .build();
        if (accountRoleEntity == null) {
            throw new ValidationException("Account role not found");
        }
        sellerRepository.save(sellerEntity);
        accountRoleRepository.save(accountRoleEntity);
    }

    @Override
    public void approveProduct(String productId) {
        var product = productRepository.findById(productId).orElseThrow(() -> new ValidationException("Product not found"));
        product.setVerified(true);
        try {
            productRepository.save(product);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to approve product", ex);
        }
    }


}