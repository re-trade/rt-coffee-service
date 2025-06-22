package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.entity.ProductEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ProductListPriceHistoryResponse {
    private ProductEntity product;

    private List<ProductPriceHistoryResponse> productPriceHistory;
}
