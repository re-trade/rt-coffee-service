package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderComboResponse {
    private String comboId;
    private String sellerId;
    private String sellerName;
    private String sellerAvatarUrl;
    private String orderStatusId;
    private String orderStatus;
    private BigDecimal grandPrice;
    private Set<CustomerOrderItemResponse> items;
}
