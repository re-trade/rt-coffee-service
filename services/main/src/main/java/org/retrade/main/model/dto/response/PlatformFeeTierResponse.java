package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformFeeTierResponse {
    private String id;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal feeRate;
    private String description;
}
