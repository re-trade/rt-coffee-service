package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.request.BrandRequest;
import org.retrade.main.model.dto.response.BrandResponse;

import java.util.List;

public interface BrandService {
    BrandResponse createBrand(BrandRequest request);

    BrandResponse updateBrand(String id, BrandRequest request);

    PaginationWrapper<List<BrandResponse>> getAllBrands(QueryWrapper queryWrapper);

    PaginationWrapper<List<BrandResponse>> getAllBrandByCategoriesList(QueryWrapper queryWrapper);

    List<BrandResponse> getAllBrandNoPaging();
}
