package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.constant.IdentityVerifiedStatusEnum;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SellerRepository extends BaseJpaRepository<SellerEntity, String> {
    Optional<SellerEntity> findByAccount(AccountEntity account);
    boolean existsByIdentityNumberIgnoreCase(@NonNull String identityNumber);
    @Modifying
    @Query("""
        UPDATE sellers s
        SET s.avgVote = (
            SELECT COALESCE(AVG(p.avgVote), 0)
            FROM products p
            WHERE p.seller = s
        )
        WHERE s.id IN (
            SELECT DISTINCT p.seller.id
            FROM products p
            WHERE p.id IN (
                SELECT DISTINCT r.product.id
                FROM product_reviews r
                WHERE r.updatedDate > :lastSync
            )
        )
    """)
    void updateSellerAverageRatings(@Param("lastSync") LocalDateTime lastSync);


    @Modifying
    @Query("""
        DELETE FROM sellers s
        WHERE s.identityVerified = :status
          AND s.createdDate <= :expiredDate
          AND NOT EXISTS (
              SELECT 1 FROM account_roles ar
              JOIN ar.role r
              WHERE ar.account = s.account
                AND r.code = 'ROLE_SELLER'
          )
    """)
    int deleteByIdentityVerifiedAndNotSellerAndExpired(
            @Param("status") IdentityVerifiedStatusEnum status,
            @Param("expiredDate") LocalDateTime expiredDate
    );

}
