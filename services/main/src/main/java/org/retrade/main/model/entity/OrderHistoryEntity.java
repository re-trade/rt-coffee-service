package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "order_histories")
public class OrderHistoryEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = OrderComboEntity.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_combo_id", nullable = false)
    private OrderComboEntity orderCombo;
    @ManyToOne(targetEntity = SellerEntity.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", nullable = false)
    private SellerEntity seller;
    @Column(name = "status", nullable = false)
    private Boolean status;
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    @ManyToOne(targetEntity = OrderStatusEntity.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "old_order_status", nullable = false)
    private OrderStatusEntity oldOrderStatus;
    @ManyToOne(targetEntity = OrderStatusEntity.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "new_order_status", nullable = false)
    private OrderStatusEntity newOrderStatus;
}
