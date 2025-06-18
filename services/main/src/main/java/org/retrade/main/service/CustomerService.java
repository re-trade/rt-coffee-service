package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.main.model.dto.request.CustomerContactRequest;
import org.retrade.main.model.dto.request.UpdateCustomerProfileRequest;
import org.retrade.main.model.dto.request.UpdatePhoneRequest;
import org.retrade.main.model.dto.response.CustomerContactResponse;
import org.retrade.main.model.dto.response.CustomerResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CustomerService {
    CustomerResponse getCurrentCustomerProfile();

    CustomerResponse updateCustomerProfile(UpdateCustomerProfileRequest request);

    PaginationWrapper<List<CustomerResponse>> getAllCustomers(QueryWrapper queryWrapper);

    void updateUserAvatar(String avatarUrl);

    PaginationWrapper<List<CustomerContactResponse>> getCustomerContacts(QueryWrapper queryWrapper);

    CustomerContactResponse getCustomerContactById(String id);

    CustomerContactResponse createCustomerContact(CustomerContactRequest request);

    CustomerContactResponse updateCustomerContact(String id, CustomerContactRequest request);

    CustomerContactResponse removeCustomerContact(String id);

    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    CustomerResponse updateCustomerPhonenumber(UpdatePhoneRequest request);
}
