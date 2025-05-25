package org.retrade.voucher.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "voucher_category_restrictions")
public class VoucherCategoryRestrictionEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = VoucherEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voucher_id", nullable = false)
    private VoucherEntity voucher;
    
    @Column(name = "category", nullable = false, length = 100)
    private String category;
}
