package org.retrade.main.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.UpdateEmailRequest;
import org.retrade.main.model.dto.request.UpdatePasswordRequest;
import org.retrade.main.model.dto.request.UpdateUsernameRequest;
import org.retrade.main.model.dto.response.*;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.AccountRoleEntity;
import org.retrade.main.model.entity.CustomerEntity;
import org.retrade.main.model.message.EmailNotificationMessage;
import org.retrade.main.repository.jpa.AccountRepository;
import org.retrade.main.repository.jpa.AccountRoleRepository;
import org.retrade.main.repository.jpa.CustomerRepository;
import org.retrade.main.service.AccountService;
import org.retrade.main.service.JwtService;
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
    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtils authUtils;
    private final CustomerRepository customerRepository;
    private final AccountRoleRepository accountRoleRepository;

    @Override
    public AccountResponse getMe() {
        AccountEntity currentAccount = authUtils.getUserAccountFromAuthentication();
        return mapToAccountResponse(currentAccount);
    }

    @Override
    public boolean checkUsernameExisted(String username) {
        return accountRepository.existsByUsername(username);
    }

    @Override
    public boolean checkEmailExisted(String email) {
        return accountRepository.existsByEmail(email);
    }

    @Override
    public AccountDetailResponse getAccountById(String id) {
        AccountEntity currentAccount = authUtils.getUserAccountFromAuthentication();
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Account not found with id: " + id));
        if (!currentAccount.getId().equals(id) && !AuthUtils.convertAccountToRole(currentAccount).contains("ROLE_ADMIN")) {
            throw new ValidationException("Access denied: You can only access your own account");
        }
        return mapToAccountDetailResponse(account);
    }

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public void updatePassword(UpdatePasswordRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getHashPassword())) {
            throw new ValidationException("Current password is incorrect");
        }
        if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
            throw new ValidationException("New password and confirm password do not match");
        }
        account.setHashPassword(passwordEncoder.encode(request.getNewPassword()));
        try {
            accountRepository.save(account);
        } catch (Exception e) {
            throw new ActionFailedException("Error while updating password", e);
        }
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
    public PaginationWrapper<List<AccountBaseResponse>> getAllAccounts(QueryWrapper queryWrapper) {
        AccountEntity currentAccount = authUtils.getUserAccountFromAuthentication();

        if (!AuthUtils.convertAccountToRole(currentAccount).contains("ROLE_ADMIN")) {
            throw new ValidationException("Access denied: Admin role required");
        }
        Page<AccountEntity> accountPage = accountRepository.queryAny(queryWrapper, queryWrapper.pagination());
        List<AccountBaseResponse> accountResponses = accountPage.getContent().stream()
                .map(this::mapToAccountBaseResponse)
                .collect(Collectors.toList());
        return new PaginationWrapper.Builder<List<AccountBaseResponse>>()
                .setData(accountResponses)
                .setPage(accountPage.getNumber())
                .setSize(accountPage.getSize())
                .setTotalPages(accountPage.getTotalPages())
                .setTotalElements((int) accountPage.getTotalElements())
                .build();
    }

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public AccountResponse updateEmail(UpdateEmailRequest request) {
        var email = request.getNewEmail();
        var emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(email)) {
            throw new ValidationException("Email input is not valid");
        }
        var account = authUtils.getUserAccountFromAuthentication();
        if (!passwordEncoder.matches(request.getPasswordConfirm(), account.getHashPassword())) {
            throw new ValidationException("Password does not match");
        }
        account.setEmail(email);
        try {
            var result = accountRepository.save(account);
            return mapToAccountResponse(result);
        } catch (Exception e) {
            throw new ActionFailedException("Error while updating email", e);
        }
    }
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    @Override
    public AccountResponse updateUsername(UpdateUsernameRequest updateRequest, HttpServletRequest request, HttpServletResponse response) {
        var username = updateRequest.username();
        if (username.length() < 3 || username.length() > 32) {
            throw new ValidationException("Username must be between 3 and 32 characters");
        }
        if (accountRepository.existsByUsername(username)) {
            throw new ValidationException("Username already exists");
        }
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.isChangedUsername()) {
            throw new ValidationException("User can only change username once per ever");
        }
        if (!passwordEncoder.matches(updateRequest.passwordConfirm(), account.getHashPassword())) {
            throw new ValidationException("Password does not match");
        }
        account.setUsername(username);
        account.setChangedUsername(true);
        try {
            var result = accountRepository.save(account);
            jwtService.removeAuthToken(request, response);
            return mapToAccountResponse(result);
        } catch (Exception e) {
            throw new ActionFailedException("Error while updating email", e);
        }
    }

    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    @Override
    public void banAccount(String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ValidationException("Account not found with ID: " + accountId));
        account.setLocked(true);
        var accountRoles = account.getAccountRoles();
        accountRoles.forEach(accountRole -> {
            accountRole.setEnabled(false);
        });
        try {
            accountRepository.save(account);
            accountRoleRepository.saveAll(accountRoles);
        } catch (Exception e) {
            throw new ActionFailedException("Error while banning account", e);
        }
    }

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public void unbanAccount(String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ValidationException("Account not found with ID: " + accountId));
        account.setLocked(false);
        var accountRoles = account.getAccountRoles();
        accountRoles.forEach(accountRole -> {
            accountRole.setEnabled(true);
        });
        try {
            accountRepository.save(account);
            accountRoleRepository.saveAll(accountRoles);
        } catch (Exception e) {
            throw new ActionFailedException("Error while unbanning account", e);
        }
    }

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public AccountResponse disableCustomerAccount(String customerId) {

        var roles = authUtils.getRolesFromAuthUser();
        if (!roles.contains("ROLE_ADMIN")) {
            throw new ValidationException("User does not have permission to approve seller");
        }

        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ValidationException("Customer not found with ID: " + customerId));

        AccountEntity account = customer.getAccount();
        Set<AccountRoleEntity> accountRoles = account.getAccountRoles();

        Optional<AccountRoleEntity> customerRole = accountRoles.stream()
                .filter(accountRole -> "ROLE_CUSTOMER".equals(accountRole.getRole().getCode()))
                .findFirst();

        if (customerRole.isEmpty()) {
            throw new ValidationException("Customer role not found for account ID: " + account.getId());
        }
        account.setEnabled(false);
        AccountRoleEntity roleToUpdate = customerRole.get();
        roleToUpdate.setEnabled(false);
        try {
            accountRoleRepository.save(roleToUpdate);
            accountRepository.save(account);
            return mapToAccountResponse(account);
        } catch (Exception e) {
            throw new ActionFailedException("Error while disabling account", e);
        }
    }

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public AccountResponse enableCustomerAccount(String customerId) {
        var roles = authUtils.getRolesFromAuthUser();
        if (!roles.contains("ROLE_ADMIN")) {
            throw new ValidationException("User does not have permission to approve seller");
        }

        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ValidationException("Customer not found with ID: " + customerId));

        AccountEntity account = customer.getAccount();
        Set<AccountRoleEntity> accountRoles = account.getAccountRoles();

        Optional<AccountRoleEntity> customerRole = accountRoles.stream()
                .filter(accountRole -> "ROLE_CUSTOMER".equals(accountRole.getRole().getCode()))
                .findFirst();

        if (customerRole.isEmpty()) {
            throw new ValidationException("Customer role not found for account ID: " + account.getId());
        }
        account.setEnabled(false);
        AccountRoleEntity roleToUpdate = customerRole.get();
        roleToUpdate.setEnabled(true);
        try {
            accountRoleRepository.save(roleToUpdate);
            accountRepository.save(account);
            return mapToAccountResponse(account);
        } catch (Exception e) {
            throw new ActionFailedException("Error while enabling account", e);
        }
    }

    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    @Override
    public void banSellerAccount(String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ValidationException("Account not found with ID: " + accountId));
        var accountRoles = account.getAccountRoles();
        accountRoles.stream().filter(accountRole -> "ROLE_SELLER".equals(accountRole.getRole().getCode())).forEach(accountRole -> {
            accountRole.setEnabled(false);
        });
        try {
            accountRoleRepository.saveAll(accountRoles);
        } catch (Exception e) {
            throw new ActionFailedException("Error while banning seller", e);
        }
    }

    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    @Override
    public void unbanSellerAccount(String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ValidationException("Account not found with ID: " + accountId));
        var accountRoles = account.getAccountRoles();
        accountRoles.stream().filter(accountRole -> "ROLE_SELLER".equals(accountRole.getRole().getCode())).forEach(accountRole -> {
            accountRole.setEnabled(true);
        });
        try {
            accountRoleRepository.saveAll(accountRoles);
        } catch (Exception e) {
            throw new ActionFailedException("Error while unbanning seller", e);
        }
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
                .changedUsername(account.isChangedUsername())
                .lastLogin(account.getLastLogin())
                .roles(AuthUtils.convertAccountToRole(account));
        return builder.build();
    }

    private AccountBaseResponse mapToAccountBaseResponse(AccountEntity account) {
        var builder = AccountBaseResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .enabled(account.isEnabled())
                .locked(account.isLocked())
                .using2FA(account.isUsing2FA())
                .joinInDate(account.getJoinInDate())
                .changedUsername(account.isChangedUsername())
                .lastLogin(account.getLastLogin())
                .roles(AuthUtils.convertAccountToRoleResponse(account));
        return builder.build();
    }

    private AccountDetailResponse mapToAccountDetailResponse(AccountEntity account) {
        var customer = account.getCustomer();
        var seller = account.getSeller();
        CustomerBaseResponse customerProfile = null;
        SellerBaseResponse sellerProfile = null;
        if (customer != null) {
            customerProfile = CustomerBaseResponse.builder()
                    .id(customer.getId())
                    .firstName(customer.getFirstName())
                    .lastName(customer.getLastName())
                    .phone(customer.getPhone())
                    .address(customer.getAddress())
                    .avatarUrl(customer.getAvatarUrl())
                    .username(customer.getAccount().getUsername())
                    .email(customer.getAccount().getEmail())
                    .gender(customer.getGender())
                    .lastUpdate(customer.getUpdatedDate().toLocalDateTime())
                    .build();
        }
        if (seller != null) {
            sellerProfile = SellerBaseResponse.builder()
                    .id(seller.getId())
                    .shopName(seller.getShopName())
                    .description(seller.getDescription())
                    .avatarUrl(seller.getAvatarUrl())
                    .email(seller.getEmail())
                    .background(seller.getBackground())
                    .phoneNumber(seller.getPhoneNumber())
                    .verified(seller.getVerified())
                    .identityVerifiedStatus(seller.getIdentityVerified())
                    .createdAt(seller.getCreatedDate().toLocalDateTime())
                    .updatedAt(seller.getUpdatedDate().toLocalDateTime())
                    .addressLine(seller.getAddressLine())
                    .district(seller.getDistrict())
                    .ward(seller.getWard())
                    .state(seller.getState())
                    .build();
        }
        return AccountDetailResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .enabled(account.isEnabled())
                .locked(account.isLocked())
                .using2FA(account.isUsing2FA())
                .joinInDate(account.getJoinInDate())
                .changedUsername(account.isChangedUsername())
                .lastLogin(account.getLastLogin())
                .roles(AuthUtils.convertAccountToRoleResponse(account))
                .customerProfile(customerProfile)
                .sellerProfile(sellerProfile)
                .build();
    }
}
