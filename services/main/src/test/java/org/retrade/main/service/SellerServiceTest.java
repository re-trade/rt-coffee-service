package org.retrade.main.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.IdentityVerifiedStatusEnum;
import org.retrade.main.model.dto.request.ApproveSellerRequest;
import org.retrade.main.model.dto.request.SellerRegisterRequest;
import org.retrade.main.model.dto.request.SellerUpdateRequest;
import org.retrade.main.model.dto.response.SellerBaseResponse;
import org.retrade.main.model.dto.response.SellerRegisterResponse;
import org.retrade.main.model.entity.*;
import org.retrade.main.model.message.CCCDVerificationMessage;
import org.retrade.main.model.message.CCCDVerificationResultMessage;
import org.retrade.main.model.other.SellerWrapperBase;
import org.retrade.main.repository.jpa.AccountRoleRepository;
import org.retrade.main.repository.jpa.RoleRepository;
import org.retrade.main.repository.jpa.SellerRepository;
import org.retrade.main.service.impl.SellerServiceImpl;
import org.retrade.main.util.AuthUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private MessageProducerService messageProducerService;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AccountRoleRepository accountRoleRepository;

    @InjectMocks
    private SellerServiceImpl sellerService;

    private AccountEntity mockAccount;
    private SellerRegisterRequest registerRequest;
    private SellerEntity sellerEntity;
    private RoleEntity roleEntity;

    @BeforeEach
    void setUp() {
        // Mock tài khoản người dùng hiện tại
        mockAccount = AccountEntity.builder()
                .email("user@example.com")
                .username("test_user")
                .enabled(true)
                .hashPassword("hashed_password")
                .secret(UUID.randomUUID().toString())
                .joinInDate(LocalDateTime.now())
                .balance(BigDecimal.ZERO)
                .build();
        mockAccount.setId(UUID.randomUUID().toString()); // Gán id cho AccountEntity

        // Mô phỏng account chưa là seller và đã là customer
        CustomerEntity customer = new CustomerEntity();
        mockAccount.setCustomer(customer);
        mockAccount.setSeller(null);

        // Mock SellerEntity
        sellerEntity = SellerEntity.builder()
                .shopName("Phuc Shop")
                .description("Best seller ever")
                .avgVote(0.0) // Khởi tạo avgVote để tránh null
                .addressLine("123 ABC")
                .district("District 1")
                .ward("Ward 2")
                .state("HCM")
                .email("seller@shop.com")
                .phoneNumber("0123456789")
                .identityNumber("123456789")
                .avatarUrl("https://avatar.img")
                .background("https://bg.img")
                .frontSideIdentityCard("example")
                .backSideIdentityCard("example")
                .identityVerified(IdentityVerifiedStatusEnum.INIT)
                .verified(false)
                .account(mockAccount)
                .build();
        sellerEntity.setId(UUID.randomUUID().toString()); // Gán id sau khi build

        // Request từ client gửi lên
        registerRequest = SellerRegisterRequest.builder()
                .shopName("Phuc Shop")
                .description("Best seller ever")
                .addressLine("123 ABC")
                .district("District 1")
                .ward("Ward 2")
                .state("HCM")
                .email("seller@shop.com")
                .phoneNumber("0123456789")
                .identityNumber("123456789")
                .avatarUrl("https://avatar.img")
                .background("https://bg.img")
                .build();

        // Mock RoleEntity
        roleEntity = new RoleEntity();
        roleEntity.setCode("ROLE_SELLER");
    }
    @Test
    void createSeller_Success() {
        // Arrange
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(mockAccount);
        when(sellerRepository.existsByIdentityNumberIgnoreCase(anyString())).thenReturn(false);
        when(sellerRepository.save(any(SellerEntity.class))).thenReturn(sellerEntity);

        // Act
        SellerRegisterResponse response = sellerService.createSeller(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(sellerEntity.getId(), response.getId());
        assertEquals("Phuc Shop", response.getShopName());
        verify(sellerRepository, times(1)).save(any(SellerEntity.class));
    }

    @Test
    void createSeller_IdentityNumberExists_ThrowsValidationException() {
        // Arrange
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(mockAccount);
        when(sellerRepository.existsByIdentityNumberIgnoreCase(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> sellerService.createSeller(registerRequest));
        verify(sellerRepository, never()).save(any(SellerEntity.class));
    }

    @Test
    void createSeller_NotCustomer_ThrowsValidationException() {
        // Arrange
        mockAccount.setCustomer(null);
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(mockAccount);

        // Act & Assert
        assertThrows(ValidationException.class, () -> sellerService.createSeller(registerRequest));
        verify(sellerRepository, never()).save(any(SellerEntity.class));
    }

    @Test
    void createSeller_AlreadySeller_ThrowsValidationException() {
        // Arrange
        mockAccount.setSeller(sellerEntity);
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(mockAccount);

        // Act & Assert
        assertThrows(ValidationException.class, () -> sellerService.createSeller(registerRequest));
        verify(sellerRepository, never()).save(any(SellerEntity.class));
    }

    @Test
    void approveSeller_Success_Approve() {
        // Arrange
        when(authUtils.getRolesFromAuthUser()).thenReturn(Set.of("ROLE_ADMIN"));
        when(sellerRepository.findById(anyString())).thenReturn(Optional.of(sellerEntity));
        when(roleRepository.findByCode("ROLE_SELLER")).thenReturn(Optional.of(roleEntity));
        when(sellerRepository.save(any(SellerEntity.class))).thenReturn(sellerEntity);
        when(accountRoleRepository.save(any(AccountRoleEntity.class))).thenReturn(new AccountRoleEntity());

        ApproveSellerRequest request = new ApproveSellerRequest(sellerEntity.getId(), true, true);

        // Act
        sellerService.approveSeller(request);

        // Assert
        verify(sellerRepository, times(1)).save(sellerEntity);
        verify(accountRoleRepository, times(1)).save(any(AccountRoleEntity.class));
        assertTrue(sellerEntity.getVerified());
    }

    @Test
    void approveSeller_NotAdmin_ThrowsValidationException() {
        // Arrange
        when(authUtils.getRolesFromAuthUser()).thenReturn(Set.of("ROLE_USER"));

        ApproveSellerRequest request = new ApproveSellerRequest(sellerEntity.getId(), true, false);

        // Act & Assert
        assertThrows(ValidationException.class, () -> sellerService.approveSeller(request));
        verify(sellerRepository, never()).save(any(SellerEntity.class));
    }

    @Test
    void approveSeller_SellerNotFound_ThrowsValidationException() {
        // Arrange
        when(authUtils.getRolesFromAuthUser()).thenReturn(Set.of("ROLE_ADMIN"));
        when(sellerRepository.findById(anyString())).thenReturn(Optional.empty());

        ApproveSellerRequest request = new ApproveSellerRequest(sellerEntity.getId(), true, false);

        // Act & Assert
        assertThrows(ValidationException.class, () -> sellerService.approveSeller(request));
        verify(sellerRepository, never()).save(any(SellerEntity.class));
    }

    @Test
    void getSellers_Success() {
        // Arrange
        QueryWrapper queryWrapper = QueryWrapper.builder()
                .wrapSort(PageRequest.of(0, 10))
                .build();
        Page<SellerEntity> page = new PageImpl<>(List.of(sellerEntity), PageRequest.of(0, 10), 1);
        when(sellerRepository.query(any(QueryWrapper.class), any(), any())).thenReturn((PaginationWrapper<List<?>>) page);

        PaginationWrapper<List<SellerBaseResponse>> response = sellerService.getSellers(queryWrapper);

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals(sellerEntity.getId(), response.getData().getFirst().getId());
        verify(sellerRepository, times(1)).query((Map<String, QueryFieldWrapper>) any(), any(), any());
    }

    @Test
    void cccdSubmit_Success() {
        // Arrange
        mockAccount.setSeller(sellerEntity);
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(mockAccount);
        when(sellerRepository.save(any(SellerEntity.class))).thenReturn(sellerEntity);
        doNothing().when(messageProducerService).sendCCCDForVerified(any(CCCDVerificationMessage.class));

        // Act
        SellerRegisterResponse response = sellerService.cccdSubmit("front.jpg", "back.jpg");

        // Assert
        assertNotNull(response);
        assertEquals(sellerEntity.getId(), response.getId());
        assertEquals(IdentityVerifiedStatusEnum.WAITING, sellerEntity.getIdentityVerified());
        verify(sellerRepository, times(1)).save(sellerEntity);
        verify(messageProducerService, times(1)).sendCCCDForVerified(any(CCCDVerificationMessage.class));
    }

    @Test
    void cccdSubmit_AlreadyVerified_ThrowsValidationException() {
        // Arrange
        sellerEntity.setIdentityVerified(IdentityVerifiedStatusEnum.VERIFIED);
        mockAccount.setSeller(sellerEntity);
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(mockAccount);

        // Act & Assert
        assertThrows(ValidationException.class, () -> sellerService.cccdSubmit("front.jpg", "back.jpg"));
        verify(sellerRepository, never()).save(any(SellerEntity.class));
    }

    @Test
    void updateSellerProfile_Success() {
        // Arrange
        mockAccount.setSeller(sellerEntity);
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(mockAccount);
        when(sellerRepository.save(any(SellerEntity.class))).thenReturn(sellerEntity);

        SellerUpdateRequest updateRequest = SellerUpdateRequest.builder()
                .shopName("Updated Shop")
                .description("Updated description")
                .addressLine("456 XYZ")
                .district("District 2")
                .ward("Ward 3")
                .state("HCM")
                .email("updated@shop.com")
                .phoneNumber("0987654321")
                .avatarUrl("https://newavatar.img")
                .background("https://newbg.img")
                .build();

        // Act
        SellerBaseResponse response = sellerService.updateSellerProfile(updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Shop", response.getShopName());
        assertEquals("456 XYZ", response.getAddressLine());
        verify(sellerRepository, times(1)).save(sellerEntity);
    }

    @Test
    void updateSellerProfile_NotSeller_ThrowsValidationException() {
        // Arrange
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(mockAccount);

        SellerUpdateRequest updateRequest = SellerUpdateRequest.builder().build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> sellerService.updateSellerProfile(updateRequest));
        verify(sellerRepository, never()).save(any(SellerEntity.class));
    }

    @Test
    void updateVerifiedSeller_Success() {
        CCCDVerificationResultMessage message = new CCCDVerificationResultMessage(sellerEntity.getId(), true, "");
        when(sellerRepository.findById(anyString())).thenReturn(Optional.of(sellerEntity));
        when(sellerRepository.save(any(SellerEntity.class))).thenReturn(sellerEntity);

        sellerService.updateVerifiedSeller(message);

        assertEquals(IdentityVerifiedStatusEnum.VERIFIED, sellerEntity.getIdentityVerified());
        verify(sellerRepository, times(1)).save(sellerEntity);
    }

    @Test
    void getSellerBaseInfoById_Success() {
        when(sellerRepository.findById(anyString())).thenReturn(Optional.of(sellerEntity));

        Optional<SellerWrapperBase> response = sellerService.getSellerBaseInfoById(sellerEntity.getId());

        assertTrue(response.isPresent());
        assertEquals(sellerEntity.getId(), response.get().sellerId());
        assertEquals(mockAccount.getEmail(), response.get().email());
    }

    @Test
    void getSellerDetails_Success() {
        sellerEntity.setVerified(true);
        when(sellerRepository.findById(anyString())).thenReturn(Optional.of(sellerEntity));

        SellerBaseResponse response = sellerService.getSellerDetails(sellerEntity.getId());

        assertNotNull(response);
        assertEquals(sellerEntity.getId(), response.getId());
        assertEquals(sellerEntity.getShopName(), response.getShopName());
    }

    @Test
    void getSellerDetails_NotVerified_ThrowsValidationException() {
        sellerEntity.setVerified(false);
        when(sellerRepository.findById(anyString())).thenReturn(Optional.of(sellerEntity));

        assertThrows(ValidationException.class, () -> sellerService.getSellerDetails(sellerEntity.getId()));
    }

    @Test
    void getMySellers_Success() {
        mockAccount.setSeller(sellerEntity);
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(mockAccount);

        SellerBaseResponse response = sellerService.getMySellers();

        // Assert
        assertNotNull(response);
        assertEquals(sellerEntity.getId(), response.getId());
    }

    @Test
    void banSeller_Success() {
        // Arrange
        when(authUtils.getRolesFromAuthUser()).thenReturn(Set.of("ROLE_ADMIN"));
        when(sellerRepository.findById(anyString())).thenReturn(Optional.of(sellerEntity));
        when(sellerRepository.save(any(SellerEntity.class))).thenReturn(sellerEntity);

        // Act
        SellerBaseResponse response = sellerService.banSeller(sellerEntity.getId());

        // Assert
        assertNotNull(response);
        assertFalse(sellerEntity.getVerified());
        verify(sellerRepository, times(1)).save(sellerEntity);
    }

    @Test
    void unbanSeller_Success() {
        // Arrange
        when(authUtils.getRolesFromAuthUser()).thenReturn(Set.of("ROLE_ADMIN"));
        when(sellerRepository.findById(anyString())).thenReturn(Optional.of(sellerEntity));
        when(sellerRepository.save(any(SellerEntity.class))).thenReturn(sellerEntity);

        // Act
        SellerBaseResponse response = sellerService.unbanSeller(sellerEntity.getId());

        // Assert
        assertNotNull(response);
        assertTrue(sellerEntity.getVerified());
        verify(sellerRepository, times(1)).save(sellerEntity);
    }
}
