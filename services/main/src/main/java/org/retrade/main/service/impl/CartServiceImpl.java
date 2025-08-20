package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.config.common.CartConfig;
import org.retrade.main.model.constant.ProductStatusEnum;
import org.retrade.main.model.dto.request.CartRequest;
import org.retrade.main.model.dto.response.CartGroupResponse;
import org.retrade.main.model.dto.response.CartItemResponse;
import org.retrade.main.model.dto.response.CartResponse;
import org.retrade.main.model.entity.CartEntity;
import org.retrade.main.model.entity.CartItemEntity;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.retrade.main.repository.jpa.ProductRepository;
import org.retrade.main.repository.jpa.SellerRepository;
import org.retrade.main.repository.redis.CartRepository;
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
    public CartResponse addToCart(CartRequest request) {
        String userId = getCurrentUserId();

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy sản phẩm"));

        String shopId = product.getSeller().getId();

        CartEntity cart = getOrCreateCart(userId);

        Set<CartItemEntity> itemsInShop = cart.getShopItems().computeIfAbsent(shopId, _ -> new HashSet<>());

        int totalItemsInCart = cart.getShopItems().values().stream()
                .mapToInt(Set::size)
                .sum();

        if (totalItemsInCart >= cartConfig.getMaxItemsPerCart()) {
            throw new ValidationException("Giỏ hàng của bạn đã đạt giới hạn. Tối đa: %d sản phẩm " + cartConfig.getMaxItemsPerCart());
        }

        Optional<CartItemEntity> existingItem = itemsInShop.stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItemEntity item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            if (item.getQuantity() > product.getQuantity()) {
                throw new ValidationException(String.format("Bạn đã chọn quá số lượng hiện có. Còn lại: %d", product.getQuantity()));
            }
            item.setUpdatedAt(LocalDateTime.now());
        } else {
            CartItemEntity newItem = CartItemEntity.builder()
                    .productId(request.getProductId())
                    .addedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .quantity(request.getQuantity())
                    .build();
            if (newItem.getQuantity() > product.getQuantity()) {
                throw new ValidationException(String.format("Bạn đã chọn quá số lượng hiện có. Còn lại: %d", product.getQuantity()));
            }
            itemsInShop.add(newItem);
        }
        cart.setLastUpdated(LocalDateTime.now());

        try {
            cart = cartRepository.save(cart);
            return mapToCartResponse(cart);
        } catch (Exception e) {
            throw new ActionFailedException("Thêm sản phẩm vào giỏ hàng thất bại", e);
        }
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(CartRequest request) {
        String userId = getCurrentUserId();

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy giỏ hảng"));

        var product = productRepository.findById(request.getProductId()).orElseThrow(() -> new ValidationException("Không tìm thấy sản phẩm"));

        boolean updated = false;
        for (Set<CartItemEntity> shopItems : cart.getShopItems().values()) {
            for (CartItemEntity item : shopItems) {
                if (item.getProductId().equals(request.getProductId())) {
                    item.setUpdatedAt(LocalDateTime.now());
                    item.setQuantity(request.getQuantity());
                    if (item.getQuantity() > product.getQuantity()) {
                        throw new ValidationException(String.format("Bạn đã chọn quá số lượng hiện có. Còn lại: %d", product.getQuantity()));
                    }
                    updated = true;
                    break;
                }
            }
            if (updated) break;
        }

        if (!updated) {
            throw new ValidationException("Món đồ không tồn tại trong giỏ hàng");
        }
        cart.setLastUpdated(LocalDateTime.now());

        try {
            cartRepository.save(cart);
            return mapToCartResponse(cart);
        } catch (Exception e) {
            throw new ActionFailedException("Lỗi khi cập nhập giỏ hàng", e);
        }
    }

    @Override
    @Transactional
    public CartResponse removeFromCart(String productId) {
        String userId = getCurrentUserId();

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy giỏ hàng"));

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
            throw new ValidationException("Không tìm thấy sản phẩm trong giỏ hàng");
        }
        cart.setLastUpdated(LocalDateTime.now());

        try {
            cartRepository.save(cart);
            return mapToCartResponse(cart);
        } catch (Exception e) {
            throw new ActionFailedException("Lỗi khi cập nhập giỏ hàng", e);
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
    @Transactional
    public void clearCart() {
        String userId = getCurrentUserId();

        try {
            cartRepository.deleteByUserId(userId);
        } catch (Exception e) {
            throw new ActionFailedException("Xóa giỏ hàng thất bại", e);
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
                    .sellerName(seller != null ? seller.getShopName() : "Người bán không xác định")
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
                        .productName("Không tìm thấy sản phẩm")
                        .totalPrice(BigDecimal.ZERO)
                        .addedAt(cartItem.getAddedAt())
                        .description("N/A")
                        .productAvailable(false)
                        .quantity(0)
                        .build();
            }
            return CartItemResponse.builder()
                    .productId(cartItem.getProductId())
                    .productName(product.getName())
                    .productThumbnail(product.getThumbnail())
                    .productBrand(product.getBrand().getName())
                    .totalPrice(product.getCurrentPrice())
                    .addedAt(cartItem.getAddedAt())
                    .description(product.getDescription() != null ? product.getDescription() : "N/A")
                    .productAvailable(product.getQuantity() > 0 && product.getStatus() == ProductStatusEnum.ACTIVE)
                    .productQuantity(product.getQuantity() != null ? product.getQuantity() : 0)
                    .quantity(cartItem.getQuantity())
                    .build();
        }).collect(Collectors.toSet());
    }
}
