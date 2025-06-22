package org.retrade.main.service;

import org.retrade.main.model.dto.response.ProductListPriceHistoryResponse;
import org.retrade.main.model.dto.response.ProductPriceHistoryResponse;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.ProductPriceHistoryEntity;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ProductPriceHistoryService {
    ProductPriceHistoryResponse implementProductPriceHistory(ProductEntity product);
  ProductListPriceHistoryResponse gettProductPriceHistoryList(String productId);
}
