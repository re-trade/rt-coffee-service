package org.retrade.voucher.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSimpleResponse {
    private String id;
    private String name;
    private String thumbnail;
    private BigDecimal currentPrice;
    private String sellerShopName;
}
