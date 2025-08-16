package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.SellerRevenueEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRevenueRepository extends BaseJpaRepository<SellerRevenueEntity, String> {
}
