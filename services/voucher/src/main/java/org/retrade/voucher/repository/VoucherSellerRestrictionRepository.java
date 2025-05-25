package org.retrade.voucher.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.voucher.model.entity.VoucherEntity;
import org.retrade.voucher.model.entity.VoucherSellerRestrictionEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherSellerRestrictionRepository extends BaseJpaRepository<VoucherSellerRestrictionEntity, String> {
    List<VoucherSellerRestrictionEntity> findByVoucher(VoucherEntity voucher);

    List<VoucherSellerRestrictionEntity> findByVoucherAndSellerId(VoucherEntity voucher, String sellerId);

    List<VoucherSellerRestrictionEntity> findBySellerId(String sellerId);

    void deleteByVoucher(VoucherEntity voucher);
}
