package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.UpdateCustomerProfileRequest;
import org.retrade.main.model.dto.request.UpdatePhoneRequest;
import org.retrade.main.model.dto.response.CustomerBaseResponse;
import org.retrade.main.model.dto.response.CustomerResponse;
import org.retrade.main.service.AccountService;
import org.retrade.main.service.CustomerService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Customer profile and management endpoints")
public class CustomerController {
    private final CustomerService customerService;
    private final AccountService accountService;

    @Operation(
            summary = "Get current customer profile",
            description = "Retrieve the authenticated customer's profile information. Requires CUSTOMER role.",
            security = {@SecurityRequirement(name = "bearerAuth"), @SecurityRequirement(name = "cookieAuth")}
    )
    @GetMapping("profile")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<CustomerResponse>> getCurrentCustomerProfile() {
        var result = customerService.getCurrentCustomerProfile();
        return ResponseEntity.ok(new ResponseObject.Builder<CustomerResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Customer profile retrieved successfully")
                .build());
    }

    @PutMapping("profile")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<CustomerResponse>> updateCustomerProfile(
            @Valid @RequestBody UpdateCustomerProfileRequest request) {
        var result = customerService.updateCustomerProfile(request);
        return ResponseEntity.ok(new ResponseObject.Builder<CustomerResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Customer profile updated successfully")
                .build());
    }

    @PutMapping("profile/avatar")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<Void>> updateUserAvatar(
            @RequestParam(name = "avatarUrl") String avatarUrl) {
        customerService.updateUserAvatar(avatarUrl);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Customer avatar updated successfully")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PatchMapping("phone")
    public ResponseEntity<ResponseObject<CustomerResponse>> updatePhone(@Valid @RequestBody UpdatePhoneRequest request) {
        var response = customerService.updateCustomerPhonenumber(request);
        return ResponseEntity.ok(new ResponseObject.Builder<CustomerResponse>()
                .success(true)
                .code("SUCCESS")
                .content(response)
                .messages("Account password reset successfully")
                .build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<List<CustomerResponse>>> getAllCustomers(
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();
        var result = customerService.getAllCustomers(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<CustomerResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Customers retrieved successfully")
                .build());
    }

    @GetMapping("customers-with-role")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<List<CustomerBaseResponse>>> getAllCustomersWithRole(
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();
        var result = customerService.getAllCustomersWithStatus(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<CustomerBaseResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Customers retrieved successfully")
                .build());
    }

    @PutMapping("{id}/disable-customer")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<Void>> disableCustomer(@PathVariable String id) {
        accountService.disableCustomerAccount(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("ban customer with" + id + "successfully")
                .build());
    }
    @PutMapping("{id}/enable-customer")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<Void>> enableCustomer(@PathVariable String id) {
        accountService.enableCustomerAccount(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("unban customer with" + id + "successfully")
                .build());
    }

}
