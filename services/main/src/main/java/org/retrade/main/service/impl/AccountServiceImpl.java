package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.UpdatePasswordRequest;
import org.retrade.main.model.dto.response.AccountResponse;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.message.EmailNotificationMessage;
import org.retrade.main.repository.AccountRepository;
import org.retrade.main.service.AccountService;
import org.retrade.main.service.MessageProducerService;
import org.retrade.main.util.AuthUtils;
import org.retrade.main.util.TokenUtils;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final MessageProducerService messageProducerService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtils authUtils;

    @Override
    public AccountResponse getMe() {
        AccountEntity currentAccount = authUtils.getUserAccountFromAuthentication();
        return mapToAccountResponse(currentAccount);
    }

    @Override
    public AccountResponse getAccountById(String id) {
        AccountEntity currentAccount = authUtils.getUserAccountFromAuthentication();
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Account not found with id: " + id));
        if (!currentAccount.getId().equals(id) && !AuthUtils.convertAccountToRole(currentAccount).contains("ROLE_ADMIN")) {
            throw new ValidationException("Access denied: You can only access your own account");
        }
        return mapToAccountResponse(account);
    }


    @Override
    @Transactional
    public void updatePassword(String id, UpdatePasswordRequest request) {
        AccountEntity currentAccount = authUtils.getUserAccountFromAuthentication();
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Account not found with id: " + id));
        if (!currentAccount.getId().equals(id) && !AuthUtils.convertAccountToRole(currentAccount).contains("ROLE_ADMIN")) {
            throw new ValidationException("Access denied: You can only update your own account");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getHashPassword())) {
            throw new ValidationException("Current password is incorrect");
        }
        if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
            throw new ValidationException("New password and confirm password do not match");
        }
        account.setHashPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public void deleteAccount(String id) {
        AccountEntity currentAccount = authUtils.getUserAccountFromAuthentication();
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Account not found with id: " + id));
        if (!currentAccount.getId().equals(id) && !AuthUtils.convertAccountToRole(currentAccount).contains("ROLE_ADMIN")) {
            throw new ValidationException("Access denied: You can only delete your own account");
        }
        account.setEnabled(false);
        account.setLocked(true);
        accountRepository.save(account);
    }

    @Override
    public void resetPassword(String id) {
        var account = accountRepository.findById(id).orElseThrow(() -> new ValidationException("Account not found with id: " + id));
        var passwordGen = TokenUtils.generatePassword(12);
        account.setHashPassword(passwordEncoder.encode(passwordGen));
        try {
            accountRepository.save(account);
            Map<String, Object> templateVars = new HashMap<>();
            EmailNotificationMessage emailMessage = EmailNotificationMessage.builder()
                    .to(account.getEmail())
                    .subject("Reset Password")
                    .templateName("reset-password")
                    .templateVariables(templateVars)
                    .messageId(UUID.randomUUID().toString())
                    .retryCount(0)
                    .build();
            messageProducerService.sendEmailNotification(emailMessage);
        } catch (Exception e) {
            throw new ActionFailedException("Error while resetting password", e);
        }
    }

    @Override
    public PaginationWrapper<List<AccountResponse>> getAllAccounts(QueryWrapper queryWrapper) {
        AccountEntity currentAccount = authUtils.getUserAccountFromAuthentication();

        if (!AuthUtils.convertAccountToRole(currentAccount).contains("ROLE_ADMIN")) {
            throw new ValidationException("Access denied: Admin role required");
        }
        Page<AccountEntity> accountPage = accountRepository.queryAny(queryWrapper, queryWrapper.pagination());
        List<AccountResponse> accountResponses = accountPage.getContent().stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());

        return new PaginationWrapper.Builder<List<AccountResponse>>()
                .setData(accountResponses)
                .setPage(accountPage.getNumber())
                .setSize(accountPage.getSize())
                .setTotalPages(accountPage.getTotalPages())
                .setTotalElements((int) accountPage.getTotalElements())
                .build();
    }

    private AccountResponse mapToAccountResponse(AccountEntity account) {
        var builder = AccountResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .enabled(account.isEnabled())
                .locked(account.isLocked())
                .using2FA(account.isUsing2FA())
                .joinInDate(account.getJoinInDate())
                .roles(AuthUtils.convertAccountToRole(account));
        return builder.build();
    }
}
