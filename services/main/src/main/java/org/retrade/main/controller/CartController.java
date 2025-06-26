package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.CartRequest;
import org.retrade.main.model.dto.response.CartResponse;
import org.retrade.main.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("carts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_CUSTOMER')")
@Tag(name = "Shopping Cart", description = "Shopping cart management endpoints - requires CUSTOMER role")
public class CartController {
    private final CartService cartService;

    @Operation(
            summary = "Add item to cart",
            description = "Add a product to the shopping cart with specified quantity. Requires CUSTOMER role.",
            security = {@SecurityRequirement(name = "bearerAuth"), @SecurityRequirement(name = "cookieAuth")}
    )
    @PostMapping("items")
    public ResponseEntity<ResponseObject<CartResponse>> addToCart(
            @Parameter(description = "Product and quantity to add") @Valid @RequestBody CartRequest request) {
        var result = cartService.addToCart(request);
        return ResponseEntity.ok(new ResponseObject.Builder<CartResponse>()
                .success(true)
                .code("ITEM_ADDED_TO_CART")
                .content(result)
                .messages("Item added to cart successfully")
                .build());
    }

    @PutMapping("items")
    public ResponseEntity<ResponseObject<CartResponse>> updateCartItem(
            @Valid @RequestBody CartRequest request) {
        var result = cartService.updateCartItem(request);
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

    @Operation(
            summary = "Get shopping cart",
            description = "Retrieve the current user's shopping cart with all items and details.",
            security = {@SecurityRequirement(name = "bearerAuth"), @SecurityRequirement(name = "cookieAuth")}
    )
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
