package org.retrade.main.service;

import org.retrade.main.model.dto.response.ProductListPriceHistoryResponse;
import org.retrade.main.model.dto.response.ProductPriceHistoryResponse;
import org.retrade.main.model.entity.ProductEntity;

import java.math.BigDecimal;
import java.util.List;


public interface ProductPriceHistoryService {

    List<ProductPriceHistoryResponse> getProductPriceHistoryList(String productId);
}
