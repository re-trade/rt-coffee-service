package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.ReportSellerEntity;

import java.util.List;

public interface ReportSellerRepository extends BaseJpaRepository<ReportSellerEntity, String> {
    List<ReportSellerEntity> findBySellerId(String sellerId);
}
