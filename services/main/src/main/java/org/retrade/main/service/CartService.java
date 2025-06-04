package org.retrade.main.service;

import org.retrade.main.model.dto.request.AddToCartRequest;
import org.retrade.main.model.dto.request.UpdateCartItemRequest;
import org.retrade.main.model.dto.response.CartResponse;
import org.retrade.main.model.dto.response.CartSummaryResponse;

public interface CartService {
    CartResponse addToCart(AddToCartRequest request);
    
    CartResponse updateCartItem(String productId);
    
    CartResponse removeFromCart(String productId);
    
    CartResponse getCart();
    
    CartSummaryResponse getCartSummary();
    
    void clearCart();
}
