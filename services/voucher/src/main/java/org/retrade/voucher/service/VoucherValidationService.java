package org.retrade.voucher.service;

import org.retrade.voucher.model.dto.request.ApplyVoucherRequest;
import org.retrade.voucher.model.dto.request.ValidateVoucherRequest;
import org.retrade.voucher.model.dto.response.VoucherValidationResponse;

public interface VoucherValidationService {
    VoucherValidationResponse validateVoucher(ValidateVoucherRequest request);
    
    VoucherValidationResponse applyVoucher(ApplyVoucherRequest request);
}
