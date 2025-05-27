package org.retrade.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.AddToCartRequest;
import org.retrade.main.model.dto.request.UpdateCartItemRequest;
import org.retrade.main.model.dto.response.CartResponse;
import org.retrade.main.model.dto.response.CartSummaryResponse;
import org.retrade.main.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_CUSTOMER')")
public class CartController {
    private final CartService cartService;

    @PostMapping("items")
    public ResponseEntity<ResponseObject<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request) {
        var result = cartService.addToCart(request);
        return ResponseEntity.ok(new ResponseObject.Builder<CartResponse>()
                .success(true)
                .code("ITEM_ADDED_TO_CART")
                .content(result)
                .messages("Item added to cart successfully")
                .build());
    }

    @PutMapping("items/{productId}")
    public ResponseEntity<ResponseObject<CartResponse>> updateCartItem(
            @PathVariable String productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        var result = cartService.updateCartItem(productId, request);
        return ResponseEntity.ok(new ResponseObject.Builder<CartResponse>()
                .success(true)
                .code("CART_ITEM_UPDATED")
                .content(result)
                .messages("Cart item updated successfully")
                .build());
    }

    @DeleteMapping("items/{productId}")
    public ResponseEntity<ResponseObject<CartResponse>> removeFromCart(
            @PathVariable String productId) {
        var result = cartService.removeFromCart(productId);
        return ResponseEntity.ok(new ResponseObject.Builder<CartResponse>()
                .success(true)
                .code("ITEM_REMOVED_FROM_CART")
                .content(result)
                .messages("Item removed from cart successfully")
                .build());
    }

    @GetMapping
    public ResponseEntity<ResponseObject<CartResponse>> getCart() {
        var result = cartService.getCart();
        return ResponseEntity.ok(new ResponseObject.Builder<CartResponse>()
                .success(true)
                .code("CART_RETRIEVED")
                .content(result)
                .messages("Cart retrieved successfully")
                .build());
    }

    @GetMapping("summary")
    public ResponseEntity<ResponseObject<CartSummaryResponse>> getCartSummary() {
        var result = cartService.getCartSummary();
        return ResponseEntity.ok(new ResponseObject.Builder<CartSummaryResponse>()
                .success(true)
                .code("CART_SUMMARY_RETRIEVED")
                .content(result)
                .messages("Cart summary retrieved successfully")
                .build());
    }

    @DeleteMapping
    public ResponseEntity<ResponseObject<Void>> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("CART_CLEARED")
                .messages("Cart cleared successfully")
                .build());
    }
}
