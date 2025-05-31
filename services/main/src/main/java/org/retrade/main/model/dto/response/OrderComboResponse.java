package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderComboResponse {
    private String comboId;
    private String sellerId;
    private String sellerName;
    private BigDecimal grandPrice;
    private String status;
    private List<String> itemIds;
}
