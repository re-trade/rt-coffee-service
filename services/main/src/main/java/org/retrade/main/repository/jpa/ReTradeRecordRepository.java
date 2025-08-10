package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.ReTradeRecordEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReTradeRecordRepository extends BaseJpaRepository<ReTradeRecordEntity, String> {
    @Query("""
            SELECT COALESCE(sum(rr.quantity), 0) FROM retrade_records rr
            WHERE rr.orderItem.id = :orderItemId
    """)
    Integer sumQuantityByOrderItemId(@Param("orderItemId") String orderItemId);

    Optional<ReTradeRecordEntity> findByProduct_Id(String id);

}
