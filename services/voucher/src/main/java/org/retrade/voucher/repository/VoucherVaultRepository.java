package org.retrade.voucher.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.voucher.model.entity.VoucherEntity;
import org.retrade.voucher.model.entity.VoucherVaultEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherVaultRepository extends BaseJpaRepository<VoucherVaultEntity, String> {
    List<VoucherVaultEntity> findByAccountId(String accountId);
    
    List<VoucherVaultEntity> findByVoucher(VoucherEntity voucher);
    
    Optional<VoucherVaultEntity> findByAccountIdAndVoucher(String accountId, VoucherEntity voucher);
    
    List<VoucherVaultEntity> findByAccountIdAndStatus(String accountId, String status);
}
