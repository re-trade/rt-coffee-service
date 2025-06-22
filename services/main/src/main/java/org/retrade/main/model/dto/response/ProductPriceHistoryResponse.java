package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.entity.ProductEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductPriceHistoryResponse {

    private BigDecimal oldPrice;

    private BigDecimal newPrice;

    private LocalDateTime fromDate;

    private LocalDateTime toDate;
}
