package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;
import org.retrade.main.model.constant.DeliveryTypeEnum;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "order_combo_deliveries")
public class OrderComboDeliveryEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = OrderComboEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_combo_id", nullable = false)
    private OrderComboEntity orderCombo;
    @Column(name = "delivery_code", nullable = false)
    private String deliveryCode;
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false)
    private DeliveryTypeEnum deliveryType;
}
