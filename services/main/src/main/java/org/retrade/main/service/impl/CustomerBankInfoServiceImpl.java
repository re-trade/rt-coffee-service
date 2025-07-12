package org.retrade.main.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.CustomerBankInfoRequest;
import org.retrade.main.model.dto.response.CustomerBankInfoResponse;
import org.retrade.main.model.entity.CustomerBankInfoEntity;
import org.retrade.main.repository.CustomerBankInfoRepository;
import org.retrade.main.service.CustomerBankInfoService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerBankInfoServiceImpl implements CustomerBankInfoService {
    private final CustomerBankInfoRepository customerBankInfoRepository;
    private final AuthUtils authUtils;

    @Override
    public CustomerBankInfoResponse getCustomerBankInfoById(String id) {
        var result = customerBankInfoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Customer bank info not found for ID: " + id));
        var account = authUtils.getUserAccountFromAuthentication();
        var customer = account.getCustomer();
        if (customer != null && customer.getId().equals(result.getCustomer().getId()) ) {
            return wrapCustomerBankResponse(result);
        }
        throw new ValidationException("Customer bank info not found for ID: " + id);
    }

    @Override
    public CustomerBankInfoResponse createCustomerBankInfo(CustomerBankInfoRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        var customer = account.getCustomer();
        if (customer == null) {
            throw new ValidationException("User is not a customer, please register customer or contact with Admin");
        }
        var entity = CustomerBankInfoEntity.builder()
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .bankCode(request.getBankCode())
                .customer(customer)
                .userBankName(request.getUserBankName())
                .build();
        try {
            var result = customerBankInfoRepository.save(entity);
            return wrapCustomerBankResponse(result);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to create customer bank info", ex);
        }
    }

    @Override
    public CustomerBankInfoResponse updateCustomerBankInfo(CustomerBankInfoRequest request, String id) {
        var entity = getCustomerBankInfoEntityById(id);
        entity.setBankName(request.getBankName());
        entity.setAccountNumber(request.getAccountNumber());
        entity.setBankCode(request.getBankCode());
        entity.setUserBankName(request.getUserBankName());
        try {
            var result = customerBankInfoRepository.save(entity);
            return wrapCustomerBankResponse(result);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to create customer bank info", ex);
        }
    }

    @Override
    public PaginationWrapper<List<CustomerBankInfoResponse>> getCustomerBankInfos(QueryWrapper queryWrapper) {
            return customerBankInfoRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                return getPredicate(param, root, criteriaBuilder, predicates);
            }, (items) -> {
                var list = items.map(this::wrapCustomerBankResponse).stream().toList();
                return new PaginationWrapper.Builder<List<CustomerBankInfoResponse>>()
                        .setPaginationInfo(items)
                        .setData(list)
                        .build();
            });
    }

    @Override
    public PaginationWrapper<List<CustomerBankInfoResponse>> getUserCustomerBankInfos(QueryWrapper queryWrapper) {
        var account = authUtils.getUserAccountFromAuthentication();
        var customer = account.getCustomer();
        if (customer == null) {
            throw new ValidationException("User is not a customer, please register customer or contact with Admin");
        }
        return customerBankInfoRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("customer"), customer));
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::wrapCustomerBankResponse).stream().toList();
            return new PaginationWrapper.Builder<List<CustomerBankInfoResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    public CustomerBankInfoResponse removeCustomerBankInfo(String id) {
        var entity = getCustomerBankInfoEntityById(id);
        try {
            customerBankInfoRepository.delete(entity);
            return wrapCustomerBankResponse(entity);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to remove customer bank info", ex);
        }
    }


    private CustomerBankInfoEntity getCustomerBankInfoEntityById(String id) {
        var account = authUtils.getUserAccountFromAuthentication();
        var customer = account.getCustomer();
        if (customer == null) {
            throw new ValidationException("User is not a customer, please register customer or contact with Admin");
        }
        var entity = customerBankInfoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Customer bank info not found for ID: " + id));
        if (!customer.getId().equals(entity.getCustomer().getId())) {
            throw new ValidationException("Customer bank info not found for ID: " + id);
        }
        return entity;
    }

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<CustomerBankInfoEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = customerBankInfoRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private CustomerBankInfoResponse wrapCustomerBankResponse (CustomerBankInfoEntity customerBankInfoEntity) {
        return CustomerBankInfoResponse.builder()
                .id(customerBankInfoEntity.getId())
                .bankName(customerBankInfoEntity.getBankName())
                .accountNumber(customerBankInfoEntity.getAccountNumber())
                .bankCode(customerBankInfoEntity.getBankCode())
                .userBankName(customerBankInfoEntity.getUserBankName())
                .build();
    }
}
