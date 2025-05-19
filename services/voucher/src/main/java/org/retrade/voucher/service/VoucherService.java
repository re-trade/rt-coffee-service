package org.retrade.voucher.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.voucher.model.dto.request.CreateVoucherRequest;
import org.retrade.voucher.model.dto.request.UpdateVoucherRequest;
import org.retrade.voucher.model.dto.response.VoucherResponse;
import org.retrade.voucher.model.entity.VoucherEntity;

import java.util.List;

public interface VoucherService {
    VoucherResponse createVoucher(CreateVoucherRequest request);
    
    VoucherResponse updateVoucher(String id, UpdateVoucherRequest request);
    
    void deleteVoucher(String id);
    
    VoucherResponse getVoucherById(String id);
    
    VoucherResponse getVoucherByCode(String code);
    
    List<VoucherResponse> getActiveVouchers();
    
    PaginationWrapper<List<VoucherResponse>> getVouchers(QueryWrapper queryWrapper);
    
    VoucherEntity getVoucherEntityById(String id);
    
    VoucherEntity getVoucherEntityByCode(String code);
}
