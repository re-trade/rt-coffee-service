package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.CreateRetradeRequest;
import org.retrade.main.model.dto.response.CreateRetradeResponse;
import org.retrade.main.model.dto.response.ProductHistoryResponse;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductHistoryService {
    PaginationWrapper<List<ProductHistoryResponse>> getProductHistoryByProductId(String productId, QueryWrapper queryWrapper);

    @Transactional(rollbackFor = {ValidationException.class, ActionFailedException.class, Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    CreateRetradeResponse createRetradeProduct(CreateRetradeRequest request);
}
