package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.CustomerBankInfoRequest;
import org.retrade.main.model.dto.request.UpdateCustomerProfileRequest;
import org.retrade.main.model.dto.request.UpdatePhoneRequest;
import org.retrade.main.model.dto.response.CustomerBankInfoResponse;
import org.retrade.main.model.dto.response.CustomerBaseMetricResponse;
import org.retrade.main.model.dto.response.CustomerResponse;
import org.retrade.main.service.AccountService;
import org.retrade.main.service.CustomerBankInfoService;
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
    private final CustomerBrvice customerBankInfoService;
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
                .messages("Lấy thông tin hồ sơ khách hàng thành công")
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
                .messages("Cập nhật hồ sơ khách hàng thành công")
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
                .messages("Cập nhật ảnh đại diện khách hàng thành công")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PatchMapping("phone")
    public ResponseEntity<ResponseObject<CustomerResponse>> updatePhone(@Valid @RequestBody UpdatePhoneRequest request) {
        var response = customerService.updateCustomerPhoneNumber(request);
        return ResponseEntity.ok(new ResponseObject.Builder<CustomerResponse>()
                .success(true)
                .code("SUCCESS")
                .content(response)
                .messages("Cập nhật số điện thoại khách hàng thành công")
                .build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
                .messages("Lấy danh sách khách hàng thành công")
                .build());
    }

    @PutMapping("{id}/disable")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<Void>> disableCustomer(@PathVariable String id) {
        accountService.disableCustomerAccount(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Khóa tài khoản khách hàng với ID: " + id + "thành công")
                .build());
    }
    @PutMapping("{id}/enable")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<Void>> enableCustomer(@PathVariable String id) {
        accountService.enableCustomerAccount(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Mở khóa tài khoản khách hàng với ID: " + id + "thành công")
                .build());
    }

    @GetMapping("me/bank-info/{id}")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<CustomerBankInfoResponse>> getCustomerBankById(@PathVariable String id) {
        var result = customerBankInfoService.getCustomerBankInfoById(id);
        return ResponseEntity.ok(
                new ResponseObject.Builder<CustomerBankInfoResponse>()
                        .code("SUCCESS")
                        .success(true)
                        .content(result)
                        .messages("Lấy thông tin ngân hàng của khách hàng thành công")
                        .build()
        );
    }

    @GetMapping("me/bank-info")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<List<CustomerBankInfoResponse>>> getCustomerById(@PageableDefault Pageable pageable, @RequestParam(required = false) String q) {
        var result = customerBankInfoService.getCustomerBankInfos(QueryWrapper.builder()
                        .search(q)
                        .wrapSort(pageable)
                .build());
        return ResponseEntity.ok(
                new ResponseObject.Builder<List<CustomerBankInfoResponse>>()
                        .code("SUCCESS")
                        .success(true)
                        .unwrapPaginationWrapper(result)
                        .messages("Lấy thông tin ngân hàng của khách hàng thành công")
                        .build()
        );
    }

    @PostMapping("me/bank-info")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<CustomerBankInfoResponse>> createCustomerBankInfo(@Valid @RequestBody CustomerBankInfoRequest request) {
        var result = customerBankInfoService.createCustomerBankInfo(request);
        return ResponseEntity.ok(new ResponseObject.Builder<CustomerBankInfoResponse>()
                        .code("SUCCESS")
                        .success(true)
                        .content(result)
                        .messages("Thêm thông tin ngân hàng thành công")
                .build());
    }

    @PutMapping("me/bank-info/{id}")
    public ResponseEntity<ResponseObject<CustomerBankInfoResponse>> updateCustomerBankInfo(@PathVariable String id, @Valid @RequestBody CustomerBankInfoRequest request) {
        var result = customerBankInfoService.updateCustomerBankInfo(request, id);
        return ResponseEntity.ok(new ResponseObject.Builder<CustomerBankInfoResponse>()
                .code("SUCCESS")
                .success(true)
                .content(result)
                .messages("Cập nhật thông tin ngân hàng khách hàng thành công")
                .build());
    }

    @DeleteMapping("me/bank-info/{id}")
    public ResponseEntity<ResponseObject<CustomerBankInfoResponse>> deleteCustomerBankInfo(@PathVariable String id) {
        var result = customerBankInfoService.removeCustomerBankInfo(id);
        return ResponseEntity.ok(new ResponseObject.Builder<CustomerBankInfoResponse>()
                .code("SUCCESS")
                .success(true)
                .content(result)
                .messages("Xóa thông tin ngân hàng thành công")
                .build());
    }

    @GetMapping("metric")
    public ResponseEntity<ResponseObject<CustomerBaseMetricResponse>> getCustomerBaseMetric() {
        var result = customerService.getCustomerBaseMetric();
        return ResponseEntity.ok(new ResponseObject.Builder<CustomerBaseMetricResponse>()
                .code("SUCCESS")
                .success(true)
                .content(result)
                .messages("Lấy thông tin thống kê khách hàng thành công")
                .build());
    }
}
