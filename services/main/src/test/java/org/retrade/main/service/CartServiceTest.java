package org.retrade.main.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.config.common.CartConfig;
import org.retrade.main.model.constant.ProductConditionEnum;
import org.retrade.main.model.constant.ProductStatusEnum;
import org.retrade.main.model.dto.request.CartRequest;
import org.retrade.main.model.dto.response.CartResponse;
import org.retrade.main.model.entity.*;


import org.retrade.main.repository.jpa.ProductRepository;
import org.retrade.main.repository.jpa.SellerRepository;
import org.retrade.main.repository.redis.CartRepository;
import org.retrade.main.service.impl.CartServiceImpl;
import org.retrade.main.util.AuthUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private SellerRepository sellerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private AuthUtils authUtils;
    @Mock private CartConfig cartConfig;

    @InjectMocks
    private CartServiceImpl cartService;

    private final String USER_ID = "user123";
    private final String PRODUCT_ID = "prod456";
    private final String SHOP_ID = "shop789";

    private ProductEntity product;
    private SellerEntity seller;
    @BeforeEach
    void setUp() {
        // Tạo seller
        seller = SellerEntity.builder()
                .shopName("Test Shop")
                .avatarUrl("shop.jpg")
                .addressLine("123 Street")
                .district("District 1")
                .ward("Ward A")
                .state("State X")
                .email("shop@example.com")
                .phoneNumber("0123456789")
                .identityNumber("123456789")
                .verified(true)
                .account(new AccountEntity())
                .build();
        seller.setId("shop789");


        BrandEntity brand = new BrandEntity();
        brand.setId("brand001");
        brand.setName("TestBrand");


        product = ProductEntity.builder()
                .name("Test Product")
                .seller(seller)
                .shortDescription("Short desc")
                .description("A great product")
                .thumbnail("thumb.jpg")
                .avgVote(4.5)
                .brand(brand)
                .quantity(10)
                .condition(ProductConditionEnum.NEW)
                .model("Model X")
                .currentPrice(BigDecimal.valueOf(100000))
                .verified(true)
                .status(ProductStatusEnum.ACTIVE)
                .build();
        product.setId(PRODUCT_ID);

    }
    private void mockAuth() {
        AccountEntity account = new AccountEntity();
        account.setId(USER_ID);
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(account);
    }

    @Test
    void testAddToCart_Success() {

        CartRequest request = new CartRequest(PRODUCT_ID, 1);
        CartEntity emptyCart = CartEntity.builder()
                .customerId(USER_ID)
                .shopItems(new HashMap<>())
                .lastUpdated(LocalDateTime.now())
                .build();

        AccountEntity mockAccount = new AccountEntity();
        mockAccount.setId(USER_ID);
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(mockAccount);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(cartConfig.getMaxItemsPerCart()).thenReturn(10);
        when(cartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sellerRepository.findAllById(any())).thenReturn(List.of(seller));
        when(productRepository.findAllById(any())).thenReturn(List.of(product));

        CartResponse response = cartService.addToCart(request);

        assertEquals(USER_ID, response.getCustomerId());
        assertEquals(1, response.getCartGroupResponses().size());
        assertEquals(PRODUCT_ID,
                response.getCartGroupResponses().get(0).getItems().iterator().next().getProductId());
    }


    @Test
    void testUpdateCartItem_Success() {

        CartRequest request = new CartRequest(PRODUCT_ID, 5);

        CartItemEntity item = CartItemEntity.builder()
                .productId(PRODUCT_ID)
                .quantity(1)
                .addedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Set<CartItemEntity> items = new HashSet<>(Set.of(item));
        Map<String, Set<CartItemEntity>> shopItems = new HashMap<>();
        shopItems.put(SHOP_ID, items);

        CartEntity cart = CartEntity.builder()
                .customerId(USER_ID)
                .shopItems(shopItems)
                .lastUpdated(LocalDateTime.now())
                .build();

        AccountEntity mockAccount = new AccountEntity();
        mockAccount.setId(USER_ID);
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(mockAccount);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sellerRepository.findAllById(any())).thenReturn(List.of(seller));
        when(productRepository.findAllById(any())).thenReturn(List.of(product));


        CartResponse response = cartService.updateCartItem(request);


        assertEquals(5, response.getCartGroupResponses().get(0).getItems().iterator().next().getQuantity());
    }

    @Test
    void testRemoveFromCart_Success() {

        CartItemEntity item = CartItemEntity.builder()
                .productId(PRODUCT_ID)
                .quantity(2)
                .addedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Set<CartItemEntity> items = new HashSet<>(Set.of(item));
        Map<String, Set<CartItemEntity>> shopItems = new HashMap<>();
        shopItems.put(SHOP_ID, items);

        CartEntity cart = CartEntity.builder()
                .customerId(USER_ID)
                .shopItems(shopItems)
                .lastUpdated(LocalDateTime.now())
                .build();


        AccountEntity mockAccount = new AccountEntity();
        mockAccount.setId(USER_ID);
        when(authUtils.getUserAccountFromAuthentication()).thenReturn(mockAccount);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        //when(sellerRepository.findAllById(any())).thenReturn(List.of(seller));
        //when(productRepository.findAllById(any())).thenReturn(List.of(product));


        CartResponse response = cartService.removeFromCart(PRODUCT_ID);

        assertEquals(USER_ID, response.getCustomerId());
        assertTrue(response.getCartGroupResponses().isEmpty());
    }

    @Test
    void testRemoveFromCart_ItemNotFound() {
        mockAuth();
        CartEntity cart = CartEntity.builder()
                .customerId(USER_ID)
                .shopItems(new HashMap<>())
                .lastUpdated(LocalDateTime.now())
                .build();

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

        assertThrows(ValidationException.class, () -> cartService.removeFromCart("invalid_id"));
    }

    @Test
    void testGetCart_WhenCartExists() {
        mockAuth();
        CartEntity cart = CartEntity.builder()
                .customerId(USER_ID)
                .shopItems(new HashMap<>())
                .lastUpdated(LocalDateTime.now())
                .build();

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.getCart();
        assertEquals(USER_ID, response.getCustomerId());
    }

    @Test
    void testGetCart_WhenCartDoesNotExist() {
        mockAuth();
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        CartResponse response = cartService.getCart();
        assertEquals(USER_ID, response.getCustomerId());
    }

    @Test
    void testClearCart_Success() {
        mockAuth();
        doNothing().when(cartRepository).deleteByUserId(USER_ID);
        assertDoesNotThrow(() -> cartService.clearCart());
    }

    @Test
    void testClearCart_Failure() {
        mockAuth();
        doThrow(new RuntimeException("DB error")).when(cartRepository).deleteByUserId(USER_ID);
        assertThrows(ActionFailedException.class, () -> cartService.clearCart());
    }

    @Test
    void testAddToCart_ProductAlreadyExistsInCart_ShouldUpdateQuantity() {
        mockAuth();
        CartRequest request = new CartRequest(PRODUCT_ID, 2);

        CartItemEntity existingItem = CartItemEntity.builder()
                .productId(PRODUCT_ID)
                .quantity(3)
                .addedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Map<String, Set<CartItemEntity>> shopItems = new HashMap<>();
        shopItems.put(SHOP_ID, new HashSet<>(Set.of(existingItem)));

        CartEntity existingCart = CartEntity.builder()
                .customerId(USER_ID)
                .shopItems(shopItems)
                .lastUpdated(LocalDateTime.now())
                .build();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingCart));
        when(cartConfig.getMaxItemsPerCart()).thenReturn(10);
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sellerRepository.findAllById(any())).thenReturn(List.of(seller));
        when(productRepository.findAllById(any())).thenReturn(List.of(product));

        CartResponse response = cartService.addToCart(request);

        int quantity = response.getCartGroupResponses().get(0).getItems().iterator().next().getQuantity();
        assertEquals(5, quantity);
    }

    @Test
    void testGetCart_ProductNotFoundInDB_ShouldMarkUnavailable() {
        mockAuth();

        CartItemEntity item = CartItemEntity.builder()
                .productId("missing_product")
                .quantity(1)
                .addedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Map<String, Set<CartItemEntity>> shopItems = new HashMap<>();
        shopItems.put(SHOP_ID, new HashSet<>(Set.of(item)));

        CartEntity cart = CartEntity.builder()
                .customerId(USER_ID)
                .shopItems(shopItems)
                .lastUpdated(LocalDateTime.now())
                .build();

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(sellerRepository.findAllById(any())).thenReturn(List.of(seller));
        when(productRepository.findAllById(any())).thenReturn(Collections.emptyList());

        CartResponse response = cartService.getCart();
        assertFalse(response.getCartGroupResponses().get(0).getItems().iterator().next().getProductAvailable());
    }

    @Test
    void testClearCart_WhenCartNotExists_ShouldStillSucceed() {
        mockAuth();
        doNothing().when(cartRepository).deleteByUserId(USER_ID);
        assertDoesNotThrow(() -> cartService.clearCart());
    }

    @Test
    void testAddToCart_SaveFails_ShouldThrowActionFailedException() {
        mockAuth();
        CartRequest request = new CartRequest(PRODUCT_ID, 1);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(cartConfig.getMaxItemsPerCart()).thenReturn(10);
        when(cartRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

        assertThrows(ActionFailedException.class, () -> cartService.addToCart(request));
    }

}
