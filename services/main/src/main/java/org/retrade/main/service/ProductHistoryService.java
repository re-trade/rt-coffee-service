package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.response.ProductHistoryResponse;

import java.util.List;

public interface ProductHistoryService {
    PaginationWrapper<List<ProductHistoryResponse>> getProductHistoryByProductId(String productId, QueryWrapper queryWrapper);

    void retradeProduct(String orderItemId);
}
