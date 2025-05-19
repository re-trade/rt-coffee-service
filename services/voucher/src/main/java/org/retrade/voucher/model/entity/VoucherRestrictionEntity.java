package org.retrade.voucher.model.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "voucher_restrictions")
public class VoucherRestrictionEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = VoucherEntity.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "voucher_id", nullable = false)
    private VoucherEntity voucher;
    private String productId;
}
