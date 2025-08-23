package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.response.ProductPriceHistoryResponse;
import org.retrade.main.model.entity.ProductPriceHistoryEntity;
import org.retrade.main.repository.jpa.ProductPriceHistoryRepository;
import org.retrade.main.service.ProductPriceHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor

public class ProductPriceHistoryServiceImpl implements ProductPriceHistoryService {
    private final ProductPriceHistoryRepository productPriceHistoryRepository;

    @Override
    public List<ProductPriceHistoryResponse>  getProductPriceHistoryList(String productId) {
        List<ProductPriceHistoryEntity> historyList = productPriceHistoryRepository.findByProduct_Id(productId);
        if (historyList.isEmpty()) {
            throw new ValidationException("Sản phẩm không tồn tại");
        }
        try {
            return historyList.stream()
                    .map(productPriceHistoryEntity ->
                            ProductPriceHistoryResponse.builder()
                                    .newPrice(productPriceHistoryEntity.getNewPrice())
                                    .dateUpdate(productPriceHistoryEntity.getUpdatedDate().toLocalDateTime())
                                    .build()
                    )
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new ActionFailedException("Lấy lịch sử giá sản phẩm thất bại");
        }
    }
}
