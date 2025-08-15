package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.PlatformFeeTierEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformFeeTierRepository extends BaseJpaRepository<PlatformFeeTierEntity, String> {
    @Query("""
        SELECT t.feeRate FROM platform_fee_tiers t
        WHERE :grandPrice >= t.minPrice
        AND (t.maxPrice IS NULL OR :grandPrice <= t.maxPrice)
        ORDER BY t.minPrice DESC
    """)
    Optional<BigDecimal> findMatchingFeeRate(@Param("grandPrice") BigDecimal grandPrice);

    @Query("""
       SELECT t FROM platform_fee_tiers t
       WHERE t.minPrice <= :maxPrice
         AND (t.maxPrice IS NULL OR t.maxPrice >= :minPrice)
    """)
    List<PlatformFeeTierEntity> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                                 @Param("maxPrice") BigDecimal maxPrice);
}
