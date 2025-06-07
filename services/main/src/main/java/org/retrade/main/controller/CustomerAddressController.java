package org.retrade.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.CustomerContactRequest;
import org.retrade.main.model.dto.response.CustomerContactResponse;
import org.retrade.main.service.CustomerService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("contacts")
public class CustomerAddressController {
    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<List<CustomerContactResponse>>> getCustomerContacts(
            @PageableDefault Pageable pageable,
            @RequestParam(required = false) String q) {
        var query = QueryWrapper.builder()
                .search(q)
                .pageable(pageable)
                .build();
        var result = customerService.getCustomerContacts(query);
        return ResponseEntity.ok(new ResponseObject.Builder<List<CustomerContactResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Get Customer Contacts successful")
                .build());
    }


    @GetMapping("{id}")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<CustomerContactResponse>> getCustomerContact(@PathVariable String id) {
        var result = customerService.getCustomerContactById(id);
        return ResponseEntity.ok(new ResponseObject.Builder<CustomerContactResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Customer profile updated successfully")
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<CustomerContactResponse>> createCustomerContact(@Valid @RequestBody CustomerContactRequest request) {
        var result = customerService.createCustomerContact(request);
        return ResponseEntity.ok(new ResponseObject.Builder<CustomerContactResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Customer address updated successfully")
                .build());
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<CustomerContactResponse>> updateCustomerContact(@PathVariable String id, @Valid @RequestBody CustomerContactRequest request) {
        var result = customerService.updateCustomerContact(id, request);
        return ResponseEntity.ok(new ResponseObject.Builder<CustomerContactResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Customer profile updated successfully")
                .build());
    }
}
