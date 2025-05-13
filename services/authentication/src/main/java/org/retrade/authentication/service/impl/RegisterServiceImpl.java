package org.retrade.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.authentication.model.constant.AuthType;
import org.retrade.authentication.model.dto.request.CustomerAccountRegisterRequest;
import org.retrade.authentication.model.dto.response.CustomerAccountRegisterResponse;
import org.retrade.authentication.model.entity.AccountEntity;
import org.retrade.authentication.model.entity.CustomerProfileEntity;
import org.retrade.authentication.repository.AccountRepository;
import org.retrade.authentication.service.RegisterService;
import org.retrade.authentication.util.TokenUtils;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public CustomerAccountRegisterResponse customerRegister(CustomerAccountRegisterRequest request) {
        var accountCheck = accountRepository.findByUsername(request.getUsername());
        if (accountCheck.isPresent()) {
            throw new ValidationException("Username already exists");
        }
        var customerAccount = AccountEntity.builder()
                .username(request.getUsername())
                .hashPassword(passwordEncoder.encode(request.getPassword()))
                .authType(AuthType.INTERNAL)
                .enabled(true)
                .locked(false)
                .using2FA(false)
                .secret(TokenUtils.generateSecretKey())
                .customerProfile(CustomerProfileEntity.builder()
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .address(request.getAddress())
                        .phone(request.getPhone())
                        .avatarUrl(request.getAvatarUrl())
                        .build())
                .build();
        try {
            var result = accountRepository.save(customerAccount);
            return wrapAccountRegisterResponse(result);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to register account", ex);
        }
    }

    private CustomerAccountRegisterResponse wrapAccountRegisterResponse(AccountEntity accountEntity) {
        return CustomerAccountRegisterResponse.builder()

                .build();
    }
}
