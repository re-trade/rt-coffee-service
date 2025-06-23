package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.config.CartConfig;
import org.retrade.main.model.dto.request.AddToCartRequest;
import org.retrade.main.model.dto.response.CartGroupResponse;
import org.retrade.main.model.dto.response.CartItemResponse;
import org.retrade.main.model.dto.response.CartResponse;
import org.retrade.main.model.dto.response.CartSummaryResponse;
import org.retrade.main.model.entity.CartEntity;
import org.retrade.main.model.entity.CartItemEntity;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.retrade.main.repository.CartRepository;
import org.retrade.main.repository.ProductRepository;
import org.retrade.main.repository.SellerRepository;
import org.retrade.main.service.CartService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final AuthUtils authUtils;
    private final CartConfig cartConfig;

    @Override
    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        String userId = getCurrentUserId();

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ValidationException("Product not found"));

        String shopId = product.getSeller().getId();

        CartEntity cart = getOrCreateCart(userId);

        Set<CartItemEntity> itemsInShop = cart.getShopItems().computeIfAbsent(shopId, _ -> new HashSet<>());

        int totalItemsInCart = cart.getShopItems().values().stream()
                .mapToInt(Set::size)
                .sum();

        if (totalItemsInCart >= cartConfig.getMaxItemsPerCart()) {
            throw new ValidationException("Cart is full. Maximum items allowed: " + cartConfig.getMaxItemsPerCart());
        }

        Optional<CartItemEntity> existingItem = itemsInShop.stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItemEntity item = existingItem.get();
            item.setUpdatedAt(LocalDateTime.now());
        } else {
            CartItemEntity newItem = CartItemEntity.builder()
                    .productId(request.getProductId())
                    .addedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            itemsInShop.add(newItem);
        }
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
    public CartResponse updateCartItem(String productId) {
        String userId = getCurrentUserId();

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ValidationException("Cart not found"));

        boolean updated = false;
        for (Set<CartItemEntity> shopItems : cart.getShopItems().values()) {
            for (CartItemEntity item : shopItems) {
                if (item.getProductId().equals(productId)) {
                    item.setUpdatedAt(LocalDateTime.now());
                    updated = true;
                    break;
                }
            }
            if (updated) break;
        }

        if (!updated) {
            throw new ValidationException("Item not found in cart");
        }
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

        boolean removed = false;

        for (Iterator<Map.Entry<String, Set<CartItemEntity>>> shopEntry = cart.getShopItems().entrySet().iterator(); shopEntry.hasNext();) {
            Map.Entry<String, Set<CartItemEntity>> entry = shopEntry.next();
            Set<CartItemEntity> items = entry.getValue();

            boolean itemRemoved = items.removeIf(item -> item.getProductId().equals(productId));
            if (itemRemoved) {
                removed = true;
                if (items.isEmpty()) {
                    shopEntry.remove();
                }
                break;
            }
        }

        if (!removed) {
            throw new ValidationException("Item not found in cart");
        }
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
                .uniqueProducts(cart.getShopItems().size())
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
                .customerId(userId)
                .shopItems(new HashMap<>())
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private CartResponse mapToCartResponse(CartEntity cart) {
        var items = mapToCartGroupResponse(cart);

        return CartResponse.builder()
                .customerId(cart.getCustomerId())
                .cartGroupResponses(items)
                .lastUpdated(cart.getLastUpdated())
                .build();
    }

    private List<CartGroupResponse> mapToCartGroupResponse(CartEntity cartEntity) {
        Map<String, Set<CartItemEntity>> items = cartEntity.getShopItems();
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> sellerIds = items.keySet();
        Map<String, SellerEntity> sellerEntities = sellerRepository.findAllById(sellerIds).stream()
                .collect(Collectors.toMap(SellerEntity::getId, Function.identity()));

        List<CartGroupResponse> groups = new ArrayList<>();

        for (Map.Entry<String, Set<CartItemEntity>> entry : items.entrySet()) {
            String shopId = entry.getKey();
            Set<CartItemEntity> cartItems = entry.getValue();
            SellerEntity seller = sellerEntities.get(shopId);

            Set<CartItemResponse> itemResponses = mapToCartItemResponse(cartItems);

            CartGroupResponse group = CartGroupResponse.builder()
                    .sellerId(shopId)
                    .sellerName(seller != null ? seller.getShopName() : "Unknown Seller")
                    .sellerAvatarUrl(seller != null ? seller.getAvatarUrl() : "")
                    .items(itemResponses)
                    .build();

            groups.add(group);
        }

        return groups;
    }

    private Set<CartItemResponse> mapToCartItemResponse(Set<CartItemEntity> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) return Collections.emptySet();
        Set<String> productIds = cartItems.stream()
                .map(CartItemEntity::getProductId)
                .collect(Collectors.toSet());
        Map<String, ProductEntity> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));
        return cartItems.stream().map(cartItem -> {
            ProductEntity product = productMap.get(cartItem.getProductId());
            if (product == null) {
                return CartItemResponse.builder()
                        .productId(cartItem.getProductId())
                        .productName("Product not found")
                        .totalPrice(BigDecimal.ZERO)
                        .addedAt(cartItem.getAddedAt())
                        .discount(0.0)
                        .description("N/A")
                        .productAvailable(false)
                        .build();
            }
            return CartItemResponse.builder()
                    .productId(cartItem.getProductId())
                    .productName(product.getName())
                    .productThumbnail(product.getThumbnail())
                    .productBrand(product.getBrand())
                    .totalPrice(product.getCurrentPrice())
                    .addedAt(cartItem.getAddedAt())
                    .discount(product.getDiscount())
                    .description(product.getDescription() != null ? product.getDescription() : "N/A")
                    .productAvailable(true)
                    .build();
        }).collect(Collectors.toSet());
    }
}
