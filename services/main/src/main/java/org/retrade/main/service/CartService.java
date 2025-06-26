package org.retrade.main.service;

import org.retrade.main.model.dto.request.CartRequest;
import org.retrade.main.model.dto.response.CartResponse;

public interface CartService {
    CartResponse addToCart(CartRequest request);
    
    CartResponse updateCartItem(CartRequest request);
    
    CartResponse removeFromCart(String productId);
    
    CartResponse getCart();
    
    void clearCart();
}
