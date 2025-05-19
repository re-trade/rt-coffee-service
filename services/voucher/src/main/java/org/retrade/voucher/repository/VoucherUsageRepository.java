package org.retrade.voucher.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.voucher.model.entity.VoucherEntity;
import org.retrade.voucher.model.entity.VoucherUsageEntity;
import org.retrade.voucher.model.entity.VoucherVaultEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherUsageRepository extends BaseJpaRepository<VoucherUsageEntity, String> {
    List<VoucherUsageEntity> findByVoucher(VoucherEntity voucher);
    
    List<VoucherUsageEntity> findByVoucherVault(VoucherVaultEntity voucherVault);
    
    List<VoucherUsageEntity> findByUserId(String userId);
    
    Optional<VoucherUsageEntity> findByOrderId(String orderId);
    
    int countByVoucherAndUserId(VoucherEntity voucher, String userId);
}
