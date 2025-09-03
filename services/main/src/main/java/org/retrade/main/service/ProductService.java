package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.request.*;
import org.retrade.main.model.dto.response.*;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(String id, UpdateProductRequest request);

    RandomProductIdResponse getRandomProductId();

    ProductResponse updateProductQuantity(UpdateProductQuantityRequest request);

    void updateSellerProductStatus(UpdateProductStatusRequest request);

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

    void approveProduct(ProductApproveRequest request, String id);

    FieldAdvanceSearch filedAdvanceSearch(QueryWrapper queryWrapper);

    FieldAdvanceSearch sellerFiledAdvanceSearch(QueryWrapper queryWrapper);

    PaginationWrapper<List<ProductResponse>> searchProductBestSelling(QueryWrapper queryWrapper);

    ProductHomeStatsResponse getStatsHome();

    PaginationWrapper<List<ProductResponse>> getProductsCanRetrade(QueryWrapper queryWrapper);

    ProductRetradeBaseResponse getProductRetradeDetail(String id);
}
