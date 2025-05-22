package org.retrade.main.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.response.AccountResponse;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.CustomerEntity;
import org.retrade.main.repository.AccountRepository;
import org.retrade.main.util.AuthUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthUtils authUtils;

    @InjectMocks
    private AccountServiceImpl accountService;

    private AccountEntity testAccount;
    private CustomerEntity testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = CustomerEntity.builder()
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .address("123 Test St")
                .avatarUrl("http://example.com/avatar.jpg")
                .build();

        testAccount = AccountEntity.builder()
                .username("testuser")
                .email("test@example.com")
                .hashPassword("hashedpassword")
                .enabled(true)
                .locked(false)
                .using2FA(false)
                .joinInDate(LocalDateTime.now())
                .customer(testCustomer)
                .build();

        // Set ID manually since it's inherited from BaseSQLEntity
        testAccount.setId("test-account-id");
        testCustomer.setAccount(testAccount);
    }

    @Test
    void getMe_ShouldReturnCurrentUserAccount() {
        // Given
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(testAccount);

        // When
        AccountResponse result = accountService.getMe();

        // Then
        assertNotNull(result);
        assertEquals(testAccount.getId(), result.getId());
        assertEquals(testAccount.getUsername(), result.getUsername());
        assertEquals(testAccount.getEmail(), result.getEmail());
        assertEquals(testAccount.isEnabled(), result.isEnabled());
        assertEquals(testAccount.isLocked(), result.isLocked());
        assertEquals(testAccount.isUsing2FA(), result.isUsing2FA());

        // Verify customer profile is mapped
        assertNotNull(result.getCustomer());
        assertEquals(testCustomer.getFirstName(), result.getCustomer().getFirstName());
        assertEquals(testCustomer.getLastName(), result.getCustomer().getLastName());
        assertEquals(testCustomer.getPhone(), result.getCustomer().getPhone());
        assertEquals(testCustomer.getAddress(), result.getCustomer().getAddress());
        assertEquals(testCustomer.getAvatarUrl(), result.getCustomer().getAvatarUrl());

        verify(authUtils).getUserAccountFromAuthentication();
    }

    @Test
    void getAccountById_WhenUserAccessesOwnAccount_ShouldReturnAccount() {
        // Given
        String accountId = "test-account-id";
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(testAccount);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));

        // When
        AccountResponse result = accountService.getAccountById(accountId);

        // Then
        assertNotNull(result);
        assertEquals(testAccount.getId(), result.getId());
        assertEquals(testAccount.getUsername(), result.getUsername());
        assertEquals(testAccount.getEmail(), result.getEmail());

        verify(authUtils).getUserAccountFromAuthentication();
        verify(accountRepository).findById(accountId);
    }

    @Test
    void getAccountById_WhenUserAccessesOtherAccountWithoutAdminRole_ShouldThrowException() {
        // Given
        String otherAccountId = "other-account-id";
        AccountEntity otherAccount = AccountEntity.builder()
                .username("otheruser")
                .email("other@example.com")
                .enabled(true)
                .locked(false)
                .using2FA(false)
                .joinInDate(LocalDateTime.now())
                .build();
        otherAccount.setId(otherAccountId);

        when(authUtils.getUserAccountFromAuthentication()).thenReturn(testAccount);
        when(accountRepository.findById(otherAccountId)).thenReturn(Optional.of(otherAccount));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
            () -> accountService.getAccountById(otherAccountId));

        assertEquals("Access denied: You can only access your own account", exception.getMessage());

        verify(authUtils).getUserAccountFromAuthentication();
        verify(accountRepository).findById(otherAccountId);
    }

    @Test
    void getAccountById_WhenAccountNotFound_ShouldThrowException() {
        // Given
        String nonExistentId = "non-existent-id";
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(testAccount);
        when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
            () -> accountService.getAccountById(nonExistentId));

        assertEquals("Account not found with id: " + nonExistentId, exception.getMessage());

        verify(authUtils).getUserAccountFromAuthentication();
        verify(accountRepository).findById(nonExistentId);
    }
}
