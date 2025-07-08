package org.retrade.main.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.retrade.main.config.common.CartConfig;
import org.retrade.main.model.entity.CartEntity;
import org.retrade.main.repository.CartRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final CartConfig cartConfig;
    private final ObjectMapper objectMapper;

    private String getCartKey(String userId) {
        return cartConfig.getKeyPrefix() + userId;
    }

    @Override
    public Optional<CartEntity> findByUserId(String userId) {
        try {
            String cartKey = getCartKey(userId);
            Object cartData = redisTemplate.opsForValue().get(cartKey);
            
            if (cartData == null) {
                return Optional.empty();
            }
            
            CartEntity cart = objectMapper.readValue(cartData.toString(), CartEntity.class);
            return Optional.of(cart);
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    @Override
    public CartEntity save(CartEntity cart) {
        try {
            String cartKey = getCartKey(cart.getCustomerId());
            String cartJson = objectMapper.writeValueAsString(cart);
            
            redisTemplate.opsForValue().set(cartKey, cartJson);
            setExpiration(cart.getCustomerId(), cartConfig.getTtlDays() * 24 * 60 * 60);
            
            return cart;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to save cart", e);
        }
    }

    @Override
    public void deleteByUserId(String userId) {
        String cartKey = getCartKey(userId);
        redisTemplate.delete(cartKey);
    }

    @Override
    public boolean existsByUserId(String userId) {
        String cartKey = getCartKey(userId);
        return redisTemplate.hasKey(cartKey);
    }

    @Override
    public void setExpiration(String userId, long ttlInSeconds) {
        String cartKey = getCartKey(userId);
        redisTemplate.expire(cartKey, ttlInSeconds, TimeUnit.SECONDS);
    }
}
