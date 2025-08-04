package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBaseMetricResponse {
    private Long soldProduct;
    private Long orderPlace;
    private Long orderComplete;
    private BigDecimal walletBalance;
}
