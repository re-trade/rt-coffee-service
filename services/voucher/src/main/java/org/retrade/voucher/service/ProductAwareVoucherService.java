package org.retrade.voucher.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.voucher.model.dto.request.CreateProductAwareVoucherRequest;
import org.retrade.voucher.model.dto.response.ProductAwareVoucherResponse;
import org.retrade.voucher.model.dto.response.ProductInfoResponse;
import org.retrade.voucher.model.dto.response.ProductSimpleResponse;
import org.retrade.voucher.model.dto.response.VoucherSimpleResponse;

import java.util.List;

public interface ProductAwareVoucherService {
    ProductAwareVoucherResponse createProductAwareVoucher(CreateProductAwareVoucherRequest request);

    ProductAwareVoucherResponse getProductAwareVoucherById(String id);

    ProductAwareVoucherResponse getProductAwareVoucherByCode(String code);

    PaginationWrapper<List<ProductAwareVoucherResponse>> getProductAwareVouchers(QueryWrapper queryWrapper);

    List<ProductAwareVoucherResponse> getVouchersForProduct(String productId);

    List<ProductAwareVoucherResponse> getVouchersForCategory(String category);

    List<ProductAwareVoucherResponse> getVouchersForSeller(String sellerId);

    List<ProductInfoResponse> getApplicableProducts(String voucherCode);

    boolean isVoucherApplicableToProduct(String voucherCode, String productId);

    boolean isVoucherApplicableToProducts(String voucherCode, List<String> productIds);

    PaginationWrapper<List<VoucherSimpleResponse>> getVouchersSimple(QueryWrapper queryWrapper);

    VoucherSimpleResponse getVoucherSimpleByCode(String code);

    List<VoucherSimpleResponse> getVouchersSimpleForProduct(String productId);

    List<ProductSimpleResponse> getApplicableProductsSimple(String voucherCode);
}
