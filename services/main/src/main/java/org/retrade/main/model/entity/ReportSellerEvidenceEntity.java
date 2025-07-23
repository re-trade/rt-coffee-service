package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;
import org.retrade.main.model.constant.SenderRoleEnum;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "report_seller_evidences")
public class ReportSellerEvidenceEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = ReportSellerEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_seller_id", nullable = false)
    private ReportSellerEntity reportSeller;

    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private AccountEntity sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false)
    private SenderRoleEnum senderRole;

    @Column(name = "evidence_urls")
    private Set<String> evidenceUrls;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
