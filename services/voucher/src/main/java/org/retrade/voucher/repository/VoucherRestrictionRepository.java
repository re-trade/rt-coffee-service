package org.retrade.voucher.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.voucher.model.entity.VoucherEntity;
import org.retrade.voucher.model.entity.VoucherRestrictionEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRestrictionRepository extends BaseJpaRepository<VoucherRestrictionEntity, String> {
    List<VoucherRestrictionEntity> findByVoucher(VoucherEntity voucher);

    List<VoucherRestrictionEntity> findByVoucherAndProductId(VoucherEntity voucher, String productId);

    List<VoucherRestrictionEntity> findByProductId(String productId);

    void deleteByVoucher(VoucherEntity voucher);
}
