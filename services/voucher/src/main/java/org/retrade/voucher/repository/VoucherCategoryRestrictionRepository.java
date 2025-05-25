package org.retrade.voucher.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.voucher.model.entity.VoucherCategoryRestrictionEntity;
import org.retrade.voucher.model.entity.VoucherEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherCategoryRestrictionRepository extends BaseJpaRepository<VoucherCategoryRestrictionEntity, String> {
    List<VoucherCategoryRestrictionEntity> findByVoucher(VoucherEntity voucher);

    List<VoucherCategoryRestrictionEntity> findByVoucherAndCategory(VoucherEntity voucher, String category);

    List<VoucherCategoryRestrictionEntity> findByCategory(String category);

    void deleteByVoucher(VoucherEntity voucher);
}
