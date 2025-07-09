package org.retrade.main.service;

import org.retrade.main.model.dto.request.ApproveSellerRequest;
import org.retrade.main.model.dto.request.SellerRegisterRequest;
import org.retrade.main.model.dto.request.SellerUpdateRequest;
import org.retrade.main.model.dto.response.SellerBaseResponse;
import org.retrade.main.model.dto.response.SellerRegisterResponse;
import org.retrade.main.model.dto.response.TopSellersResponse;
import org.retrade.main.model.message.CCCDVerificationResultMessage;
import org.retrade.main.model.other.SellerWrapperBase;

import java.util.List;
import java.util.Optional;

public interface SellerService {
    SellerRegisterResponse createSeller(SellerRegisterRequest request);

    void approveSeller(ApproveSellerRequest request);

    SellerRegisterResponse cccdSubmit(String front, String back);

    SellerBaseResponse updateSellerProfile (SellerUpdateRequest request);

    void updateVerifiedSeller(CCCDVerificationResultMessage message);

    Optional<SellerWrapperBase> getSellerBaseInfoById(String sellerId);

    SellerBaseResponse getSellerDetails(String id);

    SellerBaseResponse getMySellers();

    SellerBaseResponse banSeller(String sellerId);

    SellerBaseResponse unbanSeller(String sellerId);

}
