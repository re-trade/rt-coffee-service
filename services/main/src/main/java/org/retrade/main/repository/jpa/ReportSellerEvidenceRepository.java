package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.ReportSellerEvidenceEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportSellerEvidenceRepository extends BaseJpaRepository<ReportSellerEvidenceEntity, String> {
}
