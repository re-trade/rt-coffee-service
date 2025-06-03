package org.retrade.voucher.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "vouchers")
public class VoucherEntity extends BaseSQLEntity {
    @Column(name = "code", nullable = false, unique = true)
    private String code;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    @Column(name = "type", nullable = false)
    private String type;
    @Column(name = "discount", nullable = false)
    private Double discount;
    @Column(name = "max_discount", nullable = false)
    private BigDecimal maxDiscountAmount;
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    @Column(name = "exprired_date", nullable = false)
    private LocalDateTime expiredDate;
    @Column(name = "activated", nullable = false)
    private Boolean activated;
    @Column(name = "max_uses")
    private Integer maxUses;
    @Column(name = "max_uses_per_user")
    private Integer maxUsesPerUser;
    @Column(name = "min_spend")
    private BigDecimal minSpend;
    @Column(name = "seller_id")
    private String sellerId;

}
