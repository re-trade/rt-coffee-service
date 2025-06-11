package org.retrade.main.service;

import org.retrade.main.model.dto.request.SellerRegisterRequest;
import org.retrade.main.model.dto.request.SellerUpdateRequest;
import org.retrade.main.model.dto.response.SellerBaseResponse;
import org.retrade.main.model.dto.response.SellerRegisterResponse;

public interface SellerService {
    SellerRegisterResponse createSeller(SellerRegisterRequest request, String front, String back);

    SellerBaseResponse updateSellerProfile (SellerUpdateRequest request);
}
