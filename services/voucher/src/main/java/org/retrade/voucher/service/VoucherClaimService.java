package org.retrade.voucher.service;

import org.retrade.voucher.model.dto.request.ClaimVoucherRequest;
import org.retrade.voucher.model.dto.response.VoucherClaimResponse;

import java.util.List;

public interface VoucherClaimService {
    VoucherClaimResponse claimVoucher(ClaimVoucherRequest request);
    
    List<VoucherClaimResponse> getUserVouchers(String accountId);
    
    List<VoucherClaimResponse> getUserActiveVouchers(String accountId);
    
    void markVoucherAsUsed(String vaultId, String orderId);
}
