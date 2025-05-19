package org.retrade.main.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.retrade.main.model.dto.request.CustomerAccountRegisterRequest;
import org.retrade.main.model.dto.response.CustomerAccountRegisterResponse;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.CustomerEntity;
import org.retrade.main.model.message.EmailNotificationMessage;
import org.retrade.main.model.message.UserRegistrationMessage;
import org.retrade.main.repository.AccountRepository;
import org.retrade.main.service.impl.RegisterServiceImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegisterServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private MessageProducerService messageProducerService;

    private RegisterServiceImpl registerService;

    @BeforeEach
    void setUp() {
        registerService = new RegisterServiceImpl(accountRepository, passwordEncoder, rabbitTemplate, messageProducerService);
    }

    @Test
    void testCustomerRegister() {
        // Arrange
        CustomerAccountRegisterRequest request = new CustomerAccountRegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setAddress("123 Test St");
        request.setPhone("1234567890");
        request.setAvatarUrl("https://example.com/avatar.jpg");

        when(accountRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        AccountEntity savedAccount = new AccountEntity();
        savedAccount.setId("user123");
        savedAccount.setUsername(request.getUsername());
        savedAccount.setEmail(request.getEmail());
        savedAccount.setHashPassword("encodedPassword");
        savedAccount.setEnabled(true);
        savedAccount.setLocked(false);
        savedAccount.setUsing2FA(false);
        savedAccount.setJoinInDate(LocalDateTime.now());
        savedAccount.setSecret("secretKey");

        CustomerEntity savedCustomer = new CustomerEntity();
        savedCustomer.setFirstName(request.getFirstName());
        savedCustomer.setLastName(request.getLastName());
        savedCustomer.setAddress(request.getAddress());
        savedCustomer.setPhone(request.getPhone());
        savedCustomer.setAvatarUrl(request.getAvatarUrl());
        savedCustomer.setAccount(savedAccount);

        savedAccount.setCustomer(savedCustomer);

        when(accountRepository.save(any(AccountEntity.class))).thenReturn(savedAccount);

        // Act
        CustomerAccountRegisterResponse response = registerService.customerRegister(request);

        // Assert
        assertNotNull(response);
        assertEquals(savedAccount.getId(), response.getId());
        assertEquals(savedAccount.getUsername(), response.getUsername());
        assertEquals(savedAccount.getEmail(), response.getEmail());
        assertEquals(savedCustomer.getFirstName(), response.getFirstName());
        assertEquals(savedCustomer.getLastName(), response.getLastName());
        assertEquals(savedCustomer.getAddress(), response.getAddress());
        assertEquals(savedCustomer.getPhone(), response.getPhone());
        assertEquals(savedCustomer.getAvatarUrl(), response.getAvatarUrl());

        verify(messageProducerService, times(1)).sendUserRegistration(any(UserRegistrationMessage.class));
        verify(messageProducerService, times(1)).sendEmailNotification(any(EmailNotificationMessage.class));
    }
}
