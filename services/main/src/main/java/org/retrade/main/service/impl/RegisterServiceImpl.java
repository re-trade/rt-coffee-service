package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.CustomerAccountRegisterRequest;
import org.retrade.main.model.dto.response.CustomerAccountRegisterResponse;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.AccountRoleEntity;
import org.retrade.main.model.entity.CustomerEntity;
import org.retrade.main.model.message.EmailNotificationMessage;
import org.retrade.main.model.message.UserRegistrationMessage;
import org.retrade.main.repository.AccountRepository;
import org.retrade.main.repository.RoleRepository;
import org.retrade.main.service.MessageProducerService;
import org.retrade.main.service.RegisterService;
import org.retrade.main.util.TokenUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterServiceImpl implements RegisterService {
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageProducerService messageProducerService;
    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public CustomerAccountRegisterResponse customerRegister(CustomerAccountRegisterRequest request) {
        var accountCheck = accountRepository.findByUsername(request.getUsername());
        if (accountCheck.isPresent()) {
            throw new ValidationException("Username already exists");
        }
        var roleCustomer = roleRepository.findByCode("ROLE_CUSTOMER")
                .orElseThrow(() -> new ValidationException("Role not found"));
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
        var accountRoleEntity = AccountRoleEntity.builder()
                .account(customerAccount)
                .role(roleCustomer)
                .enabled(true)
                .build();
        customerAccount.setAccountRoles(Set.of(accountRoleEntity));
        var customerProfile = CustomerEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .avatarUrl(request.getAvatarUrl())
                .gender(request.getGender())
                .account(customerAccount)
                .build();
        customerAccount.setCustomer(customerProfile);
        try {
            var result = accountRepository.save(customerAccount);
            sendRegistrationMessages(result);
            return wrapAccountRegisterResponse(result);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to register account", ex);
        }
    }

    private CustomerAccountRegisterResponse wrapAccountRegisterResponse(AccountEntity accountEntity) {
        var customerProfile = accountEntity.getCustomer();
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

    private void sendRegistrationMessages(AccountEntity accountEntity) {
        try {
            CustomerEntity customerProfile = accountEntity.getCustomer();
            UserRegistrationMessage registrationMessage = UserRegistrationMessage.builder()
                    .userId(accountEntity.getId())
                    .username(accountEntity.getUsername())
                    .email(accountEntity.getEmail())
                    .firstName(customerProfile.getFirstName())
                    .lastName(customerProfile.getLastName())
                    .registrationDate(accountEntity.getJoinInDate())
                    .messageId(UUID.randomUUID().toString())
                    .retryCount(0)
                    .build();
            messageProducerService.sendUserRegistration(registrationMessage);
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("firstName", customerProfile.getFirstName());
            templateVariables.put("lastName", customerProfile.getLastName());
            templateVariables.put("username", accountEntity.getUsername());

            EmailNotificationMessage emailMessage = EmailNotificationMessage.builder()
                    .to(accountEntity.getEmail())
                    .subject("Welcome to ReTrade!")
                    .templateName("welcome-email")
                    .templateVariables(templateVariables)
                    .messageId(UUID.randomUUID().toString())
                    .retryCount(0)
                    .build();
            messageProducerService.sendEmailNotification(emailMessage);
            log.info("Registration messages sent for user: {}", accountEntity.getUsername());
        } catch (Exception e) {
            log.error("Failed to send registration messages for user: {}", accountEntity.getUsername(), e);
        }
    }
}
