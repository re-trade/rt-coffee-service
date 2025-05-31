package org.retrade.main.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.retrade.proto.voucher.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class VoucherGrpcClient {
    
    @Value("${grpc.client.voucher-service.host:localhost}")
    private String voucherServiceHost;
    
    @Value("${grpc.client.voucher-service.port:9081}")
    private int voucherServicePort;
    
    private ManagedChannel channel;
    private GrpcVoucherServiceGrpc.GrpcVoucherServiceBlockingStub voucherServiceStub;
    
    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(voucherServiceHost, voucherServicePort)
                .usePlaintext()
                .build();
        voucherServiceStub = GrpcVoucherServiceGrpc.newBlockingStub(channel);
        log.info("Voucher gRPC client initialized for {}:{}", voucherServiceHost, voucherServicePort);
    }
    
    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                log.info("Voucher gRPC client shutdown completed");
            } catch (InterruptedException e) {
                log.warn("Voucher gRPC client shutdown interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public ValidateVoucherResponse validateVoucher(String code, String accountId, BigDecimal orderTotal, List<String> productIds) {
        try {
            ValidateVoucherRequest request = ValidateVoucherRequest.newBuilder()
                    .setCode(code)
                    .setAccountId(accountId)
                    .setOrderTotal(orderTotal.doubleValue())
                    .addAllProductIds(productIds)
                    .build();
            
            ValidateVoucherResponse response = voucherServiceStub.validateVoucher(request);
            
            log.debug("Voucher validation result for code {}: valid={}, message={}", 
                code, response.getValid(), response.getMessage());
            
            return response;
        } catch (Exception e) {
            log.error("Error validating voucher {}: {}", code, e.getMessage(), e);
            return ValidateVoucherResponse.newBuilder()
                    .setValid(false)
                    .setMessage("Failed to validate voucher: " + e.getMessage())
                    .build();
        }
    }
    
    public ApplyVoucherResponse applyVoucher(String code, String accountId, String orderId, BigDecimal orderTotal) {
        try {
            ApplyVoucherRequest request = ApplyVoucherRequest.newBuilder()
                    .setCode(code)
                    .setAccountId(accountId)
                    .setOrderId(orderId)
                    .setOrderTotal(orderTotal.doubleValue())
                    .build();
            
            ApplyVoucherResponse response = voucherServiceStub.applyVoucher(request);
            
            log.debug("Voucher application result for code {}: success={}, discount={}", 
                code, response.getSuccess(), response.getDiscountAmount());
            
            return response;
        } catch (Exception e) {
            log.error("Error applying voucher {}: {}", code, e.getMessage(), e);
            return ApplyVoucherResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to apply voucher: " + e.getMessage())
                    .setDiscountAmount(0.0)
                    .build();
        }
    }
    
    public GetVoucherByCodeResponse getVoucherByCode(String code) {
        try {
            GetVoucherByCodeRequest request = GetVoucherByCodeRequest.newBuilder()
                    .setCode(code)
                    .build();
            
            GetVoucherByCodeResponse response = voucherServiceStub.getVoucherByCode(request);
            
            log.debug("Get voucher by code result for {}: success={}", code, response.getSuccess());
            
            return response;
        } catch (Exception e) {
            log.error("Error getting voucher by code {}: {}", code, e.getMessage(), e);
            return GetVoucherByCodeResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to get voucher: " + e.getMessage())
                    .build();
        }
    }
    
    public GetUserVouchersResponse getUserVouchers(String accountId, boolean activeOnly) {
        try {
            GetUserVouchersRequest request = GetUserVouchersRequest.newBuilder()
                    .setAccountId(accountId)
                    .setActiveOnly(activeOnly)
                    .build();
            
            GetUserVouchersResponse response = voucherServiceStub.getUserVouchers(request);
            
            log.debug("Get user vouchers result for account {}: success={}, count={}", 
                accountId, response.getSuccess(), response.getVouchersCount());
            
            return response;
        } catch (Exception e) {
            log.error("Error getting user vouchers for account {}: {}", accountId, e.getMessage(), e);
            return GetUserVouchersResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to get user vouchers: " + e.getMessage())
                    .build();
        }
    }
}
