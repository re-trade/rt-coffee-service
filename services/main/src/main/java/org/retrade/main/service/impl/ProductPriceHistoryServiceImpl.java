package org.retrade.main.service.impl;

import ch.qos.logback.core.joran.spi.ActionException;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.main.model.dto.response.ProductListPriceHistoryResponse;
import org.retrade.main.model.dto.response.ProductPriceHistoryResponse;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.ProductPriceHistoryEntity;
import org.retrade.main.repository.ProductPriceHistoryRepository;
import org.retrade.main.repository.ProductRepository;
import org.retrade.main.service.ProductPriceHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service

public class ProductPriceHistoryServiceImpl implements ProductPriceHistoryService {
    private ProductPriceHistoryRepository productPriceHistoryRepository;
    private ProductRepository productRepository;

    @Override
    public ProductPriceHistoryResponse implementProductPriceHistory(ProductEntity product) {
        ProductPriceHistoryEntity productPriceHistoryEntity = new ProductPriceHistoryEntity();
        productPriceHistoryEntity.setId(product.getId());
        productPriceHistoryEntity.setNewPrice(product.getCurrentPrice());
        productPriceHistoryEntity.setToDate(LocalDateTime.now());
        productPriceHistoryRepository.save(productPriceHistoryEntity);
        return ProductPriceHistoryResponse.builder()

                .newPrice(productPriceHistoryEntity.getNewPrice())
                .toDate(productPriceHistoryEntity.getToDate())
                .build();
    }

    @Override
    public ProductListPriceHistoryResponse gettProductPriceHistoryList(String productId) {
        try {
            List<ProductPriceHistoryEntity> historyList = productPriceHistoryRepository.findByProduct_Id(productId);
            if (historyList.isEmpty()) {
                throw new ActionException("Product not exist");
            }

            ProductEntity product = historyList.getFirst().getProduct();

            List<ProductPriceHistoryResponse> priceChanges = historyList.stream()
                    .map(e -> new ProductPriceHistoryResponse(
                            e.getOldPrice(),
                            e.getNewPrice(),
                            e.getFromDate(),
                            e.getToDate()
                    ))
                    .toList();

            return ProductListPriceHistoryResponse.builder()
                    .product(product)
                    .productPriceHistory(priceChanges)
                    .build();
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to get product price history");
        }
    }


}
