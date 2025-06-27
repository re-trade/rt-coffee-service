package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "report_sellers")
public class ReportSellerEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = SellerEntity.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private SellerEntity seller;

    @Column(name = "type_report", nullable = false, columnDefinition = "TEXT")
    private String typeReport;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "resolution_status", nullable = false, columnDefinition = "TEXT")
    private String resolutionStatus;

    @Column(name = "resolution_detail", nullable = false, columnDefinition = "TEXT")
    private String resolutionDetail;

    @Column(name = "resolution_date", nullable = false)
    private Timestamp resolutionDate;

    @ManyToOne(targetEntity =  OrderComboEntity.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_id",nullable = false)
    private OrderComboEntity orderCombo;

    @ManyToOne(targetEntity =  ProductEntity.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id",nullable = false)
    private ProductEntity product;

    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private AccountEntity account;

    @ManyToOne(targetEntity = CustomerEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Column(name="image")
    private String image;
}
