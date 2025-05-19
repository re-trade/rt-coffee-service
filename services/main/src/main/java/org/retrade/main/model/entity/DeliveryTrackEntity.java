package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "delivery_tracks")
public class DeliveryTrackEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = SellerEntity.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private SellerEntity seller;
    @ManyToOne(targetEntity = OrderComboEntity.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_combo_id", nullable = false)
    private OrderComboEntity orderCombo;
    @Column(name = "status", nullable = false)
    private Boolean status;
    @Column(name = "content", nullable = false)
    private String content;
}
