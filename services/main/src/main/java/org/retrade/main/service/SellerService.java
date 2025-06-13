package org.retrade.main.service;

import org.retrade.main.model.dto.request.SellerRegisterRequest;
import org.retrade.main.model.dto.request.SellerUpdateRequest;
import org.retrade.main.model.dto.response.SellerBaseResponse;
import org.retrade.main.model.dto.response.SellerRegisterResponse;
import org.retrade.main.model.message.CCCDVerificationResultMessage;

public interface SellerService {
    SellerRegisterResponse createSeller(SellerRegisterRequest request);

    SellerRegisterResponse cccdSubmit(String front, String back);

    SellerBaseResponse updateSellerProfile (SellerUpdateRequest request);

    void updateVerifiedSeller(CCCDVerificationResultMessage message);
}
