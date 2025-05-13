package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.CustomerAccountRegisterRequest;
import org.retrade.main.model.dto.response.CustomerAccountRegisterResponse;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.CustomerProfileEntity;
import org.retrade.main.repository.AccountRepository;
import org.retrade.main.service.RegisterService;
import org.retrade.main.util.TokenUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public CustomerAccountRegisterResponse customerRegister(CustomerAccountRegisterRequest request) {
        var accountCheck = accountRepository.findByUsername(request.getUsername());
        if (accountCheck.isPresent()) {
            throw new ValidationException("Username already exists");
        }
        var customerAccount = AccountEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .hashPassword(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .locked(false)
                .using2FA(false)
                .joinInDate(LocalDateTime.now())
                .secret(TokenUtils.generateSecretKey())
                .build();
        var customerProfile = CustomerProfileEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .avatarUrl(request.getAvatarUrl())
                .account(customerAccount)
                .build();
        customerAccount.setCustomerProfile(customerProfile);
        try {
            var result = accountRepository.save(customerAccount);
            return wrapAccountRegisterResponse(result);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to register account", ex);
        }
    }

    private CustomerAccountRegisterResponse wrapAccountRegisterResponse(AccountEntity accountEntity) {
        var customerProfile = accountEntity.getCustomerProfile();
        return CustomerAccountRegisterResponse.builder()
                .id(accountEntity.getId())
                .username(accountEntity.getUsername())
                .email(accountEntity.getEmail())
                .firstName(customerProfile.getFirstName())
                .lastName(customerProfile.getLastName())
                .address(customerProfile.getAddress())
                .phone(customerProfile.getPhone())
                .avatarUrl(customerProfile.getAvatarUrl())
                .build();
    }
}
