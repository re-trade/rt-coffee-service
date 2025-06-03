package org.retrade.voucher.service;

import org.retrade.voucher.model.dto.request.ClaimVoucherRequest;
import org.retrade.voucher.model.dto.response.VoucherClaimResponse;
import org.retrade.voucher.model.dto.response.VoucherClaimSimpleResponse;

import java.util.List;

public interface VoucherClaimService {
    VoucherClaimResponse claimVoucher(ClaimVoucherRequest request);

    List<VoucherClaimResponse> getUserVouchers(String accountId);

    List<VoucherClaimResponse> getUserActiveVouchers(String accountId);

    void markVoucherAsUsed(String vaultId, String orderId);

    VoucherClaimSimpleResponse claimVoucherSimple(ClaimVoucherRequest request);

    List<VoucherClaimSimpleResponse> getUserVouchersSimple(String accountId);

    List<VoucherClaimSimpleResponse> getUserActiveVouchersSimple(String accountId);
}
