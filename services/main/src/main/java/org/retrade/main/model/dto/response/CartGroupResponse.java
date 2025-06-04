package org.retrade.main.model.dto.response;

import lombok.Builder;

import java.util.Set;

@Builder
public class CartGroupResponse {
    private String sellerId;
    private String sellerName;
    private String sellerAvatarUrl;
    private Set<CartItemResponse> items;
}
