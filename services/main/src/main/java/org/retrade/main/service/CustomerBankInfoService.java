package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.request.CustomerBankInfoRequest;
import org.retrade.main.model.dto.response.CustomerBankInfoResponse;

import java.util.List;

public interface CustomerBankInfoService {
    CustomerBankInfoResponse getCustomerBankInfoById(String id);

    CustomerBankInfoResponse createCustomerBankInfo(CustomerBankInfoRequest request);

    CustomerBankInfoResponse updateCustomerBankInfo(CustomerBankInfoRequest request, String id);

    PaginationWrapper<List<CustomerBankInfoResponse>> getCustomerBankInfos(QueryWrapper queryWrapper);

    PaginationWrapper<List<CustomerBankInfoResponse>> getUserCustomerBankInfos(QueryWrapper queryWrapper);

    CustomerBankInfoResponse removeCustomerBankInfo(String id);
}
