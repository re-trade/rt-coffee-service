package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.SellerRevenueEntity;
import org.retrade.main.model.projection.RevenueMonthProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SellerRevenueRepository extends BaseJpaRepository<SellerRevenueEntity, String> {
    @Query("SELECT COALESCE(SUM(s.platformFeeAmount), 0) FROM seller_revenues s")
    BigDecimal calculateAdminRevenue();

    @Query("""
        SELECT COALESCE(SUM(s.platformFeeAmount), 0)
        FROM seller_revenues s 
        WHERE s.createdDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal calculateAdminRevenueByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT COALESCE(SUM(s.platformFeeAmount), 0)
        FROM seller_revenues s
        WHERE YEAR(s.createdDate) = :year AND MONTH(s.createdDate) = :month
    """)
    BigDecimal calculateAdminRevenueByMonthAndYear(
            @Param("year") int year, @Param("month") int month
    );


    @Query("""
        SELECT MONTH(o.createdDate) AS month, SUM(o.platformFeeAmount) AS total
        FROM seller_revenues o
        WHERE YEAR(o.createdDate) = :year
        GROUP BY MONTH(o.createdDate)
        ORDER BY MONTH(o.createdDate)
    """)
    List<RevenueMonthProjection> getRevenuePerMonth(@Param("year") int year);

}
