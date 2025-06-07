package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartGroupResponse {
    private String sellerId;
    private String sellerName;
    private String sellerAvatarUrl;
    private Set<CartItemResponse> items;
}
