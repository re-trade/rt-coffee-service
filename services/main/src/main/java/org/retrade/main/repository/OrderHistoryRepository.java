package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.OrderHistoryEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends BaseJpaRepository<OrderHistoryEntity, String> {
    List<OrderHistoryEntity> findByOrderCombo_Id(String orderComboId);

    OrderHistoryEntity findByIdAndSeller(String id, SellerEntity seller);
}
