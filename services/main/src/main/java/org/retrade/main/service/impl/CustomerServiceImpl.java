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
import org.retrade.main.model.dto.request.CustomerContactRequest;
import org.retrade.main.model.dto.request.UpdateCustomerProfileRequest;
import org.retrade.main.model.dto.response.CustomerContactResponse;
import org.retrade.main.model.dto.response.CustomerResponse;
import org.retrade.main.model.entity.CustomerContactEntity;
import org.retrade.main.model.entity.CustomerEntity;
import org.retrade.main.repository.CustomerContactRepository;
import org.retrade.main.repository.CustomerRepository;
import org.retrade.main.service.CustomerService;
import org.retrade.main.util.AuthUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerContactRepository customerContactRepository;
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

    @Override
    public PaginationWrapper<List<CustomerContactResponse>> getCustomerContacts(QueryWrapper queryWrapper) {
        var account = authUtils.getUserAccountFromAuthentication();
        var customer = account.getCustomer();
        if (customer == null) {
            throw new ValidationException("Customer profile not found");
        }
        return customerContactRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("customer"), customer));
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::mapToCustomerContactResponse).stream().toList();
            return new PaginationWrapper.Builder<List<CustomerContactResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    public CustomerContactResponse getCustomerContactById(String id) {
        var customer = getAuthCustomer();
        var customerContact = customerContactRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Customer contact not found with id: " + id));
        if (!customerContact.getCustomer().equals(customer)) {
            throw new ValidationException("Customer contact not found with id: " + id);
        }
        return mapToCustomerContactResponse(customerContact);
    }

    @Override
    public CustomerContactResponse createCustomerContact(CustomerContactRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        var customer = account.getCustomer();
        if (customer == null) {
            throw new ValidationException("Customer profile not found");
        }
        var customerContactCreate = CustomerContactEntity.builder()
                .customerName(request.getCustomerName())
                .phone(request.getPhone())
                .state(request.getState())
                .country(request.getCountry())
                .district(request.getDistrict())
                .ward(request.getWard())
                .addressLine(request.getAddressLine())
                .name(request.getName())
                .defaulted(request.getDefaulted())
                .type(request.getType())
                .customer(customer)
                .build();
        var result = customerContactRepository.save(customerContactCreate);
        return mapToCustomerContactResponse(result);
    }

    @Override
    public CustomerContactResponse updateCustomerContact(String id, CustomerContactRequest request) {
        var customer = getAuthCustomer();
        var contactEntity = customerContactRepository.findById(id).orElseThrow(() -> new ValidationException("Customer contact not found with id: " + id));
        if (!customer.equals(contactEntity.getCustomer())) {
            throw new ValidationException("You are not authorized to update this customer contact");
        }
        contactEntity.setCustomerName(request.getCustomerName());
        contactEntity.setPhone(request.getPhone());
        contactEntity.setState(request.getState());
        contactEntity.setCountry(request.getCountry());
        contactEntity.setDistrict(request.getDistrict());
        contactEntity.setWard(request.getWard());
        contactEntity.setAddressLine(request.getAddressLine());
        contactEntity.setDefaulted(request.getDefaulted());
        contactEntity.setType(request.getType());
        var result = customerContactRepository.save(contactEntity);
        return mapToCustomerContactResponse(result);
    }

    @Override
    public CustomerContactResponse removeCustomerContact(String id) {
        var customer = getAuthCustomer();
        var contactEntity = customerContactRepository.findById(id).orElseThrow(() -> new ValidationException("Customer contact not found with id: " + id));
        if (!customer.equals(contactEntity.getCustomer())) {
            throw new ValidationException("You are not authorized to update this customer contact");
        }
        try {
            customerContactRepository.delete(contactEntity);
            return mapToCustomerContactResponse(contactEntity);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to remove customer contact", ex);
        }
    }

    private CustomerEntity getAuthCustomer() {
        var account = authUtils.getUserAccountFromAuthentication();
        var customer = account.getCustomer();
        if (customer == null) {
            throw new ValidationException("Customer profile not found");
        }
        return customer;
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

    private CustomerContactResponse mapToCustomerContactResponse(CustomerContactEntity customerContact) {
        return CustomerContactResponse.builder()
                .id(customerContact.getId())
                .customerName(customerContact.getCustomerName())
                .phone(customerContact.getPhone())
                .state(customerContact.getState())
                .country(customerContact.getCountry())
                .district(customerContact.getDistrict())
                .ward(customerContact.getWard())
                .addressLine(customerContact.getAddressLine())
                .name(customerContact.getName())
                .defaulted(customerContact.getDefaulted())
                .type(customerContact.getType())
                .build();
    }

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<CustomerContactEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = customerContactRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
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
