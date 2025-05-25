package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.UpdateCustomerProfileRequest;
import org.retrade.main.model.dto.response.CustomerResponse;
import org.retrade.main.model.entity.CustomerEntity;
import org.retrade.main.repository.CustomerRepository;
import org.retrade.main.service.CustomerService;
import org.retrade.main.util.AuthUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final AuthUtils authUtils;

    @Override
    public CustomerResponse getCurrentCustomerProfile() {
        var account = authUtils.getUserAccountFromAuthentication();
        var customer = customerRepository.findByAccount(account)
                .orElseThrow(() -> new ValidationException("Customer profile not found"));
        return mapToCustomerResponse(customer);
    }

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public CustomerResponse updateCustomerProfile(UpdateCustomerProfileRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        var customer = customerRepository.findByAccount(account)
                .orElseThrow(() -> new ValidationException("Customer profile not found"));

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        if (request.getAvatarUrl() != null) {
            customer.setAvatarUrl(request.getAvatarUrl());
        }

        try {
            var updatedCustomer = customerRepository.save(customer);
            return mapToCustomerResponse(updatedCustomer);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to update customer profile", ex);
        }
    }

    @Override
    public PaginationWrapper<List<CustomerResponse>> getAllCustomers(QueryWrapper queryWrapper) {
        Specification<CustomerEntity> spec = buildSpecification(queryWrapper);
        Page<CustomerEntity> customerPage = customerRepository.findAll(spec, queryWrapper.pagination());

        List<CustomerResponse> customerResponses = customerPage.getContent()
                .stream()
                .map(this::mapToCustomerResponse)
                .toList();

        return new PaginationWrapper.Builder<List<CustomerResponse>>()
                .setData(customerResponses)
                .setPaginationInfo(customerPage)
                .build();
    }

    @Override
    public void updateUserAvatar(String avatarUrl) {
        var account = authUtils.getUserAccountFromAuthentication();
        var customer = customerRepository.findByAccount(account)
                .orElseThrow(() -> new ValidationException("Customer profile not found"));
        customer.setAvatarUrl(avatarUrl);
        try {
            customerRepository.save(customer);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to update customer avatar", ex);
        }
    }

    private CustomerResponse mapToCustomerResponse(CustomerEntity customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .avatarUrl(customer.getAvatarUrl())
                .username(customer.getAccount().getUsername())
                .email(customer.getAccount().getEmail())
                .build();
    }

    private Specification<CustomerEntity> buildSpecification(QueryWrapper queryWrapper) {
        Specification<CustomerEntity> spec = Specification.where(null);

        var searchFields = queryWrapper.search();

        if (searchFields.containsKey("firstName")) {
            var field = searchFields.get("firstName");
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("firstName")),
                    "%" + field.getValue().toString().toLowerCase() + "%"));
        }

        if (searchFields.containsKey("lastName")) {
            var field = searchFields.get("lastName");
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("lastName")),
                    "%" + field.getValue().toString().toLowerCase() + "%"));
        }

        if (searchFields.containsKey("phone")) {
            var field = searchFields.get("phone");
            spec = spec.and((root, query, cb) ->
                cb.like(root.get("phone"),
                    "%" + field.getValue().toString() + "%"));
        }

        if (searchFields.containsKey("username")) {
            var field = searchFields.get("username");
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.join("account").get("username")),
                    "%" + field.getValue().toString().toLowerCase() + "%"));
        }
        if (searchFields.containsKey("email")) {
            var field = searchFields.get("email");
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.join("account").get("email")),
                    "%" + field.getValue().toString().toLowerCase() + "%"));
        }

        return spec;
    }
}
