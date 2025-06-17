package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "order_destinations")
public class OrderDestinationEntity extends BaseSQLEntity {
    @OneToOne(targetEntity = OrderEntity.class, fetch = FetchType.LAZY, mappedBy = "orderDestination")
    private OrderEntity order;
    @Column(name = "customer_name", length = 255, nullable = false)
    private String customerName;
    @Column(name = "phone", length = 12, nullable = false)
    private String phone;
    @Column(name = "state", length = 20)
    private String state;
    @Column(name = "country", length = 20)
    private String country;
    @Column(name = "district", length = 20)
    private String district;
    @Column(name = "ward", length = 20)
    private String ward;
    @Column(name = "address_line", length = 500)
    private String addressLine;
}
