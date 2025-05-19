package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "orders")
public class OrderEntity extends BaseSQLEntity {
    @OneToOne(fetch = FetchType.EAGER, optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private OrderDestinationEntity orderDestination;
    @Column(name = "tax_total", nullable = false)
    private BigDecimal taxTotal;
    @Column(name = "discount_total", nullable = false)
    private BigDecimal discountTotal;
    @Column(name = "sub_total", nullable = false)
    private BigDecimal subtotal;
    @Column(name = "shipping_total", nullable = false)
    private Double shippingCost;
    @Column(name = "grand_total", nullable = false)
    private BigDecimal grandTotal;
    @ManyToOne(optional = false, fetch = FetchType.EAGER, targetEntity = CustomerEntity.class)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE}, targetEntity = OrderItemEntity.class, mappedBy = "id")
    private Set<OrderItemEntity> orderItems;
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE}, targetEntity = OrderHistoryEntity.class, mappedBy = "id")
    private Set<OrderHistoryEntity> orderHistories;
}
