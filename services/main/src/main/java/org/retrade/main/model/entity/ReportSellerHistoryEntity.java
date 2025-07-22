package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "report_seller_histories")
public class ReportSellerHistoryEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = ReportSellerEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_seller_id", nullable = false)
    private ReportSellerEntity reportSeller;
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private AccountEntity admin;
    @Column(name = "action_type", length = 50, nullable = false)
    private String actionType;
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
