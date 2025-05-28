package org.retrade.main.repository;

import org.retrade.main.model.entity.CartEntity;

import java.util.Optional;

public interface CartRepository {
    Optional<CartEntity> findByUserId(String userId);
    
    CartEntity save(CartEntity cart);
    
    void deleteByUserId(String userId);
    
    boolean existsByUserId(String userId);
    
    void setExpiration(String userId, long ttlInSeconds);
}
