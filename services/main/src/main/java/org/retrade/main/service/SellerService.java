package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.request.ApproveSellerRequest;
import org.retrade.main.model.dto.request.SellerRegisterRequest;
import org.retrade.main.model.dto.request.SellerUpdateRequest;
import org.retrade.main.model.dto.response.SellerBaseMetricResponse;
import org.retrade.main.model.dto.response.SellerRegisterResponse;
import org.retrade.main.model.dto.response.SellerResponse;
import org.retrade.main.model.dto.response.SellerStatusResponse;
import org.retrade.main.model.message.CCCDVerificationResultMessage;
import org.retrade.main.model.other.SellerWrapperBase;

import java.util.List;
import java.util.Optional;

public interface SellerService {
    SellerRegisterResponse createSeller(SellerRegisterRequest request);

    void approveSeller(ApproveSellerRequest request);

    SellerBaseMetricResponse getSellerBaseMetric(String sellerId);

    PaginationWrapper<List<SellerResponse>> getSellers (QueryWrapper wrapper);

    void removeSellerProfileInit();

    SellerRegisterResponse cccdSubmit(String front, String back);

    SellerResponse updateSellerProfile (SellerUpdateRequest request);

    void updateVerifiedSeller(CCCDVerificationResultMessage message);

    Optional<SellerWrapperBase> getSellerBaseInfoById(String sellerId);

    SellerResponse getSellerDetails(String id);

    SellerStatusResponse checkSellerStatus();

    SellerResponse getMySellers();

    SellerResponse banSeller(String sellerId);

    SellerResponse unbanSeller(String sellerId);

}
