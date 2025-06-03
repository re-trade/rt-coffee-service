package org.retrade.voucher.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.voucher.model.entity.VoucherEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends BaseJpaRepository<VoucherEntity, String> {
    Optional<VoucherEntity> findByCode(String code);

    List<VoucherEntity> findByActivated(Boolean active);

    Page<VoucherEntity> findByActivated(Boolean active, Pageable pageable);
}
