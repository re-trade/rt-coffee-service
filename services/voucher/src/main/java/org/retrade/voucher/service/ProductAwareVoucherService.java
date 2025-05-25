package org.retrade.voucher.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.proto.product.ProductInfo;
import org.retrade.voucher.model.dto.request.CreateProductAwareVoucherRequest;
import org.retrade.voucher.model.dto.response.ProductAwareVoucherResponse;

import java.util.List;

public interface ProductAwareVoucherService {
    ProductAwareVoucherResponse createProductAwareVoucher(CreateProductAwareVoucherRequest request);
    
    ProductAwareVoucherResponse getProductAwareVoucherById(String id);
    
    ProductAwareVoucherResponse getProductAwareVoucherByCode(String code);
    
    PaginationWrapper<List<ProductAwareVoucherResponse>> getProductAwareVouchers(QueryWrapper queryWrapper);
    
    List<ProductAwareVoucherResponse> getVouchersForProduct(String productId);
    
    List<ProductAwareVoucherResponse> getVouchersForCategory(String category);
    
    List<ProductAwareVoucherResponse> getVouchersForSeller(String sellerId);
    
    List<ProductInfo> getApplicableProducts(String voucherCode);
    
    boolean isVoucherApplicableToProduct(String voucherCode, String productId);
    
    boolean isVoucherApplicableToProducts(String voucherCode, List<String> productIds);
}
