package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.request.CreateProductRequest;
import org.retrade.main.model.dto.request.UpdateProductRequest;
import org.retrade.main.model.dto.response.FieldAdvanceSearch;
import org.retrade.main.model.dto.response.ProductHomeStatsResponse;
import org.retrade.main.model.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(String id, UpdateProductRequest request);

    void deleteProduct(String id);

    ProductResponse getProductById(String id);

    PaginationWrapper<List<ProductResponse>> getAllProducts(QueryWrapper queryWrapper);

    PaginationWrapper<List<ProductResponse>> getProductSimilar(QueryWrapper queryWrapper);

    PaginationWrapper<List<ProductResponse>> getProductsBySeller(String sellerId, QueryWrapper queryWrapper);

    PaginationWrapper<List<ProductResponse>> getMyProducts(QueryWrapper queryWrapper);


    PaginationWrapper<List<ProductResponse>> searchProductByKeyword(QueryWrapper queryWrapper);

    List<ProductResponse> getProductsByCategory(String categoryName);

    PaginationWrapper<List<ProductResponse>> getProductsByCategory(String categoryName, QueryWrapper queryWrapper);

    void verifyProduct(String id);

    void unverifyProduct(String id);


    FieldAdvanceSearch filedAdvanceSearch(QueryWrapper queryWrapper);

    FieldAdvanceSearch sellerFiledAdvanceSearch(QueryWrapper queryWrapper);

    PaginationWrapper<List<ProductResponse>> searchProductBestSelling(QueryWrapper queryWrapper);

    ProductHomeStatsResponse getStatsHome();
}
