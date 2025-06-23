package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductPriceHistoryResponse {

    private String productId;

    private BigDecimal oldPrice;

    private BigDecimal newPrice;

    private LocalDateTime dateUpdate;

}
