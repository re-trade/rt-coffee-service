package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.ReportSellerHistoryEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportSellerHistoryRepository extends BaseJpaRepository<ReportSellerHistoryEntity, String> {
}
