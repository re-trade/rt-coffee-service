package org.retrade.voucher.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.voucher.model.entity.VoucherEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends BaseJpaRepository<VoucherEntity, String> {
    Optional<VoucherEntity> findByCode(String code);
    
    List<VoucherEntity> findByActived(Boolean active);
}
