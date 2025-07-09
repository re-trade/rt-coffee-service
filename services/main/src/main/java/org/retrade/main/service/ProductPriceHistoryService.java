package org.retrade.main.service;

import org.retrade.main.model.dto.response.ProductPriceHistoryResponse;

import java.util.List;


public interface ProductPriceHistoryService {

    List<ProductPriceHistoryResponse> getProductPriceHistoryList(String productId);
}
