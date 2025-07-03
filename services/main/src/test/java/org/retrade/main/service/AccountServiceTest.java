package org.retrade.main.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.UpdateEmailRequest;
import org.retrade.main.model.dto.request.UpdatePasswordRequest;
import org.retrade.main.model.dto.request.UpdateUsernameRequest;
import org.retrade.main.model.dto.response.AccountResponse;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.AccountRoleEntity;
import org.retrade.main.model.entity.RoleEntity;
import org.retrade.main.model.message.EmailNotificationMessage;
import org.retrade.main.repository.AccountRepository;
import org.retrade.main.service.JwtService;
import org.retrade.main.service.MessageProducerService;
import org.retrade.main.service.impl.AccountServiceImpl;
import org.retrade.main.util.AuthUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private MessageProducerService messageProducerService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthUtils authUtils;

    @InjectMocks
    private AccountServiceImpl accountService;

    private AccountEntity account;

   /* @BeforeEach
    void setUp() {
        account = new AccountEntity();
        account.setId("1");
        account.setUsername("testuser");
        account.setEmail("test@example.com");
        account.setHashPassword("encodedPassword");
        account.setEnabled(true);
        account.setLocked(false);
        account.setUsing2FA(false);
        account.setChangedUsername(false);
        account.setJoinInDate(LocalDateTime.now());
        account.setLastLogin(LocalDateTime.now());

        // Tạo RoleEntity đầy đủ để không bị lỗi
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName("Admin");
        roleEntity.setId("1");
        roleEntity.setCode("111");


        AccountRoleEntity accountRole = new AccountRoleEntity();
        accountRole.setRole(roleEntity);
        accountRole.setEnabled(Boolean.TRUE);

        Set<AccountRoleEntity> roles = new HashSet<>();
        roles.add(accountRole);

        account.setAccountRoles(roles);
    }*/
   @BeforeEach
   void setUp() {
       account = new AccountEntity();
       account.setId("1");
       account.setUsername("testuser");
       account.setEmail("test@example.com");
       account.setHashPassword("encodedPassword");
       account.setEnabled(true);
       account.setLocked(false);
       account.setUsing2FA(false);
       account.setChangedUsername(false);
       account.setJoinInDate(LocalDateTime.now());
       account.setLastLogin(LocalDateTime.now());

       RoleEntity roleEntity = new RoleEntity();
       roleEntity.setName("ROLE_USER");
       roleEntity.setId("1");
       roleEntity.setCode("111");

       AccountRoleEntity accountRole = new AccountRoleEntity();
       accountRole.setRole(roleEntity);
       accountRole.setEnabled(Boolean.TRUE);

       Set<AccountRoleEntity> roles = new HashSet<>();
       roles.add(accountRole);

       account.setAccountRoles(roles);
   }



    @Test
    void getMe_Success() {
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);

        account.getAccountRoles().forEach(role -> {
            System.out.println("Enabled: " + role.getEnabled());
            System.out.println("RoleEntity: " + role.getRole());
            System.out.println("RoleEntity name: " + (role.getRole() != null ? role.getRole().getName() : null));
        });

        AccountResponse response = accountService.getMe();

        assertNotNull(response);
        assertEquals("1", response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void checkUsernameExisted_True() {
        when(accountRepository.existsByUsername("testuser")).thenReturn(true);

        boolean exists = accountService.checkUsernameExisted("testuser");

        assertTrue(exists);
        verify(accountRepository).existsByUsername("testuser");
    }

    @Test
    void checkUsernameExisted_False() {
        when(accountRepository.existsByUsername("nonexistent")).thenReturn(false);

        boolean exists = accountService.checkUsernameExisted("nonexistent");

        assertFalse(exists);
        verify(accountRepository).existsByUsername("nonexistent");
    }

    @Test
    void getAccountById_Success_SameUser() {
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);
        when(accountRepository.findById("1")).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccountById("1");

        assertNotNull(response);
        assertEquals("1", response.getId());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void getAccountById_AccessDenied_NonAdmin() {
        AccountEntity otherAccount = new AccountEntity();
        otherAccount.setId("2");
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);
        when(accountRepository.findById("2")).thenReturn(Optional.of(otherAccount));

        assertThrows(ValidationException.class, () -> accountService.getAccountById("2"));
    }


    @Test
    void getAccountById_NotFound() {
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);
        when(accountRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> accountService.getAccountById("2"));
    }

    @Test
    void updatePassword_Success() {
        UpdatePasswordRequest request = new UpdatePasswordRequest("currentPassword", "newPassword", "newPassword");
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(accountRepository.save(account)).thenReturn(account);

        accountService.updatePassword(request);

        assertEquals("newEncodedPassword", account.getHashPassword());
        verify(accountRepository).save(account);
    }

    @Test
    void updatePassword_IncorrectCurrentPassword() {
        UpdatePasswordRequest request = new UpdatePasswordRequest("wrongPassword", "newPassword", "newPassword");
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(ValidationException.class, () -> accountService.updatePassword(request));
    }

    @Test
    void updatePassword_PasswordsDoNotMatch() {
        UpdatePasswordRequest request = new UpdatePasswordRequest("currentPassword", "newPassword", "differentPassword");
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);

        assertThrows(ValidationException.class, () -> accountService.updatePassword(request));
    }

    @Test
    void deleteAccount_Success_SameUser() {
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);
        when(accountRepository.findById("1")).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        accountService.deleteAccount("1");

        assertFalse(account.isEnabled());
        assertTrue(account.isLocked());
        verify(accountRepository).save(account);
    }

    @Test
    void deleteAccount_AccessDenied_NonAdmin() {
        AccountEntity otherAccount = new AccountEntity();
        otherAccount.setId("2");
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);
        when(accountRepository.findById("2")).thenReturn(Optional.of(otherAccount));
        //when(authUtils.convertAccountToRole(account)).thenReturn(Collections.singletonList("ROLE_USER"));

        assertThrows(ValidationException.class, () -> accountService.deleteAccount("2"));
    }

    @Test
    void resetPassword_Success() {
        AccountEntity targetAccount = new AccountEntity();
        targetAccount.setId("1");
        targetAccount.setEmail("test@example.com");
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);
        when(accountRepository.findById("1")).thenReturn(Optional.of(targetAccount));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(accountRepository.save(targetAccount)).thenReturn(targetAccount);

        accountService.resetPassword("1");

        verify(accountRepository).save(targetAccount);
        verify(messageProducerService).sendEmailNotification(any(EmailNotificationMessage.class));
    }

/*    @Test
    void getAllAccounts_Success_Admin() {
        // Arrange
        QueryWrapper queryWrapper = new QueryWrapper();
        Page<AccountEntity> page = new PageImpl<>(Collections.singletonList(account), PageRequest.of(0, 10), 1);

        // Mock the authenticated user and their role
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);
        when(authUtils.convertAccountToRole(account)).thenReturn(Collections.singletonList("ROLE_ADMIN"));

        // Mock the repository call to return a page of accounts
        when(accountRepository.queryAny(queryWrapper, PageRequest.of(0, 10))).thenReturn(page);

        // Act
        PaginationWrapper<List<AccountResponse>> response = accountService.getAllAccounts(queryWrapper);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getData(), "Response data should not be null");
        assertEquals(1, response.getData().size(), "Response should contain one account");
        assertEquals("testuser", response.getData().get(0).getUsername(), "Username should match");

        // Verify interactions
        verify(authUtils).getUserAccountFromAuthentication();
        verify(authUtils).convertAccountToRole(account);
        verify(accountRepository).queryAny(queryWrapper, PageRequest.of(0, 10));
    }

    @Test
    void getAllAccounts_AccessDenied_NonAdmin() {
        // Arrange
        QueryWrapper queryWrapper = new QueryWrapper();

        // Mock the authenticated user and their role
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);
        when(authUtils.convertAccountToRole(account)).thenReturn(Collections.singletonList("ROLE_USER"));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> accountService.getAllAccounts(queryWrapper),
                "Expected ValidationException for non-admin user"
        );

        // Verify the exception message (optional, adjust based on implementation)
        assertEquals("Access denied: Admin role required", exception.getMessage());

        // Verify interactions
        verify(authUtils).getUserAccountFromAuthentication();
        verify(authUtils).convertAccountToRole(account);
        verifyNoInteractions(accountRepository); // Repository should not be called
    }*/

    @Test
    void updateEmail_Success() {
        UpdateEmailRequest request = new UpdateEmailRequest("new@example.com", "currentPassword");
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(accountRepository.save(account)).thenReturn(account);

        AccountResponse response = accountService.updateEmail(request);

        assertEquals("new@example.com", account.getEmail());
        assertEquals("new@example.com", response.getEmail());
        verify(accountRepository).save(account);
    }

    @Test
    void updateEmail_InvalidEmail() {
        // Arrange
        UpdateEmailRequest request = new UpdateEmailRequest("invalid-email", "currentPassword");
        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> accountService.updateEmail(request),
                "Phải ném ValidationException khi email không hợp lệ"
        );
        assertEquals("Email input is not valid", exception.getMessage(), "Thông điệp lỗi phải khớp");

        verifyNoInteractions(authUtils, accountRepository, passwordEncoder);
    }

    @Test
    void updateUsername_Success() {
        UpdateUsernameRequest request = new UpdateUsernameRequest("newUsername", "currentPassword");
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(accountRepository.existsByUsername("newUsername")).thenReturn(false);
        when(accountRepository.save(account)).thenReturn(account);

        AccountResponse response = accountService.updateUsername(request, httpRequest, httpResponse);

        assertEquals("newUsername", account.getUsername());
        assertTrue(account.isChangedUsername());
        assertEquals("newUsername", response.getUsername());
        verify(jwtService).removeAuthToken(httpRequest, httpResponse);
        verify(accountRepository).save(account);
    }

    @Test
    void updateUsername_UsernameAlreadyExists() {
        UpdateUsernameRequest request = new UpdateUsernameRequest("existingUsername", "currentPassword");
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);
        when(accountRepository.existsByUsername("existingUsername")).thenReturn(true);

        assertThrows(ValidationException.class, () -> accountService.updateUsername(request, httpRequest, httpResponse));
    }

    @Test
    void updateUsername_AlreadyChanged() {
        account.setChangedUsername(true);
        UpdateUsernameRequest request = new UpdateUsernameRequest("newUsername", "currentPassword");
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);

        assertThrows(ValidationException.class, () -> accountService.updateUsername(request, httpRequest, httpResponse));
    }
}