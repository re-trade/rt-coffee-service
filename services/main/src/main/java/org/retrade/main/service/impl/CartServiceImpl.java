package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.config.CartConfig;
import org.retrade.main.model.dto.request.AddToCartRequest;
import org.retrade.main.model.dto.request.UpdateCartItemRequest;
import org.retrade.main.model.dto.response.CartItemResponse;
import org.retrade.main.model.dto.response.CartResponse;
import org.retrade.main.model.dto.response.CartSummaryResponse;
import org.retrade.main.model.entity.CartEntity;
import org.retrade.main.model.entity.CartItemEntity;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.repository.CartRepository;
import org.retrade.main.repository.ProductRepository;
import org.retrade.main.service.CartService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final AuthUtils authUtils;
    private final CartConfig cartConfig;

    @Override
    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        String userId = getCurrentUserId();

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ValidationException("Product not found"));

        if (request.getQuantity() > cartConfig.getMaxQuantityPerItem()) {
            throw new ValidationException("Quantity exceeds maximum allowed per item");
        }

        CartEntity cart = getOrCreateCart(userId);

        if (cart.getItems().size() >= cartConfig.getMaxItemsPerCart()) {
            throw new ValidationException("Cart is full. Maximum items allowed: " + cartConfig.getMaxItemsPerCart());
        }

        Optional<CartItemEntity> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItemEntity item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();

            if (newQuantity > cartConfig.getMaxQuantityPerItem()) {
                throw new ValidationException("Total quantity exceeds maximum allowed per item");
            }

            item.setQuantity(newQuantity);
            item.setUpdatedAt(LocalDateTime.now());
        } else {
            CartItemEntity newItem = CartItemEntity.builder()
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .priceSnapshot(product.getCurrentPrice())
                    .addedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            cart.getItems().add(newItem);
        }

        updateCartTotals(cart);
        cart.setLastUpdated(LocalDateTime.now());

        try {
            cart = cartRepository.save(cart);
            return mapToCartResponse(cart);
        } catch (Exception e) {
            throw new ActionFailedException("Failed to add item to cart", e);
        }
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(String productId, UpdateCartItemRequest request) {
        String userId = getCurrentUserId();

        if (request.getQuantity() > cartConfig.getMaxQuantityPerItem()) {
            throw new ValidationException("Quantity exceeds maximum allowed per item");
        }

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ValidationException("Cart not found"));

        CartItemEntity item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Item not found in cart"));

        item.setQuantity(request.getQuantity());
        item.setUpdatedAt(LocalDateTime.now());

        updateCartTotals(cart);
        cart.setLastUpdated(LocalDateTime.now());

        try {
            cartRepository.save(cart);
            return mapToCartResponse(cart);
        } catch (Exception e) {
            throw new ActionFailedException("Failed to update cart item", e);
        }
    }

    @Override
    @Transactional
    public CartResponse removeFromCart(String productId) {
        String userId = getCurrentUserId();

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ValidationException("Cart not found"));

        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        if (!removed) {
            throw new ValidationException("Item not found in cart");
        }

        updateCartTotals(cart);
        cart.setLastUpdated(LocalDateTime.now());

        try {
            cartRepository.save(cart);
            return mapToCartResponse(cart);
        } catch (Exception e) {
            throw new ActionFailedException("Failed to remove item from cart", e);
        }
    }

    @Override
    public CartResponse getCart() {
        String userId = getCurrentUserId();

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElse(createEmptyCart(userId));

        return mapToCartResponse(cart);
    }

    @Override
    public CartSummaryResponse getCartSummary() {
        String userId = getCurrentUserId();

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElse(createEmptyCart(userId));

        return CartSummaryResponse.builder()
                .totalItems(cart.getTotalItems() != null ? cart.getTotalItems() : 0)
                .totalAmount(cart.getTotalAmount() != null ? cart.getTotalAmount() : BigDecimal.ZERO)
                .uniqueProducts(cart.getItems().size())
                .build();
    }

    @Override
    @Transactional
    public void clearCart() {
        String userId = getCurrentUserId();

        try {
            cartRepository.deleteByUserId(userId);
        } catch (Exception e) {
            throw new ActionFailedException("Failed to clear cart", e);
        }
    }

    private String getCurrentUserId() {
        return authUtils.getUserAccountFromAuthentication().getId();
    }

    private CartEntity getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElse(createEmptyCart(userId));
    }

    private CartEntity createEmptyCart(String userId) {
        return CartEntity.builder()
                .userId(userId)
                .items(new ArrayList<>())
                .totalItems(0)
                .totalAmount(BigDecimal.ZERO)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private void updateCartTotals(CartEntity cart) {
        int totalItems = cart.getItems().stream()
                .mapToInt(CartItemEntity::getQuantity)
                .sum();

        BigDecimal totalAmount = cart.getItems().stream()
                .map(item -> item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalItems(totalItems);
        cart.setTotalAmount(totalAmount);
    }

    private CartResponse mapToCartResponse(CartEntity cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .toList();

        boolean hasUnavailableItems = itemResponses.stream()
                .anyMatch(item -> !item.getProductAvailable());

        return CartResponse.builder()
                .userId(cart.getUserId())
                .items(itemResponses)
                .totalItems(cart.getTotalItems())
                .totalAmount(cart.getTotalAmount())
                .lastUpdated(cart.getLastUpdated())
                .hasUnavailableItems(hasUnavailableItems)
                .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItemEntity cartItem) {
        Optional<ProductEntity> productOpt = productRepository.findById(cartItem.getProductId());

        if (productOpt.isEmpty()) {
            return CartItemResponse.builder()
                    .productId(cartItem.getProductId())
                    .productName("Product not found")
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getPriceSnapshot())
                    .totalPrice(cartItem.getPriceSnapshot().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .addedAt(cartItem.getAddedAt())
                    .productAvailable(false)
                    .build();
        }

        ProductEntity product = productOpt.get();

        return CartItemResponse.builder()
                .productId(cartItem.getProductId())
                .productName(product.getName())
                .productThumbnail(product.getThumbnail())
                .productBrand(product.getBrand())
                .sellerName(product.getSeller().getShopName())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getPriceSnapshot())
                .totalPrice(cartItem.getPriceSnapshot().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .addedAt(cartItem.getAddedAt())
                .productAvailable(true)
                .build();
    }
}
