package org.retrade.main.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.CreateProductRequest;
import org.retrade.main.model.dto.response.ProductResponse;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.CategoryEntity;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.retrade.main.repository.CategoryRepository;
import org.retrade.main.repository.ProductRepository;
import org.retrade.main.repository.SellerRepository;
import org.retrade.main.util.AuthUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuthUtils authUtils;

    @InjectMocks
    private ProductServiceImpl productService;

    private AccountEntity mockAccount;
    private SellerEntity mockSeller;
    private CategoryEntity mockCategory;
    private ProductEntity mockProduct;

    @BeforeEach
    void setUp() {
        mockAccount = AccountEntity.builder()
                .username("testuser")
                .email("test@example.com")
                .hashPassword("hashedpassword")
                .secret("secret123")
                .enabled(true)
                .locked(false)
                .using2FA(false)
                .joinInDate(java.time.LocalDateTime.now())
                .build();

        mockSeller = SellerEntity.builder()
                .shopName("Test Shop")
                .email("seller@example.com")
                .phoneNumber("1234567890")
                .identityNumber("123456789")
                .verified(true)
                .account(mockAccount)
                .build();

        mockCategory = CategoryEntity.builder()
                .name("Electronics")
                .description("Electronic products")
                .visible(true)
                .type("PRODUCT")
                .build();

        mockProduct = ProductEntity.builder()
                .name("Test Product")
                .seller(mockSeller)
                .shortDescription("Short description")
                .brand("Test Brand")
                .discount("10%")
                .model("Test Model")
                .currentPrice(new BigDecimal("99.99"))
                .categories(Set.of(mockCategory))
                .verified(false)
                .build();
    }

    @Test
    void createProduct_WithValidCategories_ShouldSucceed() {
        // Arrange
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Test Product")
                .shortDescription("Short desc")
                .brand("Test Brand")
                .model("Test Model")
                .currentPrice(new BigDecimal("99.99"))
                .categories(Set.of("Electronics"))
                .build();

        when(authUtils.getUserAccountFromAuthentication()).thenReturn(mockAccount);
        when(sellerRepository.findByAccount(mockAccount)).thenReturn(Optional.of(mockSeller));
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(mockCategory));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(mockProduct);

        // Act
        ProductResponse result = productService.createProduct(request);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("Test Brand", result.getBrand());
        assertTrue(result.getCategories().contains("Electronics"));
        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository).findByName("Electronics");
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void createProduct_WithInvalidCategories_ShouldThrowException() {
        // Arrange
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Test Product")
                .shortDescription("Short desc")
                .brand("Test Brand")
                .model("Test Model")
                .currentPrice(new BigDecimal("99.99"))
                .categories(Set.of("InvalidCategory"))
                .build();

        when(authUtils.getUserAccountFromAuthentication()).thenReturn(mockAccount);
        when(sellerRepository.findByAccount(mockAccount)).thenReturn(Optional.of(mockSeller));
        when(categoryRepository.existsByName("InvalidCategory")).thenReturn(false);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> productService.createProduct(request));

        assertTrue(exception.getMessage().contains("Invalid categories"));
        assertTrue(exception.getMessage().contains("InvalidCategory"));
        verify(categoryRepository).existsByName("InvalidCategory");
        verify(productRepository, never()).save(any(ProductEntity.class));
    }

    @Test
    void getProductsByCategory_WithValidCategory_ShouldReturnProducts() {
        // Arrange
        String categoryName = "Electronics";
        List<ProductEntity> mockProducts = List.of(mockProduct);

        when(productRepository.findByCategoryName(categoryName)).thenReturn(mockProducts);

        // Act
        List<ProductResponse> result = productService.getProductsByCategory(categoryName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productRepository).findByCategoryName(categoryName);
    }

    @Test
    void getProductsByCategory_WithNonExistentCategory_ShouldReturnEmptyList() {
        // Arrange
        String categoryName = "NonExistentCategory";

        when(productRepository.findByCategoryName(categoryName)).thenReturn(List.of());

        // Act
        List<ProductResponse> result = productService.getProductsByCategory(categoryName);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findByCategoryName(categoryName);
    }
}
