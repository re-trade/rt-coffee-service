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
    @Column(name = "address", length = 50)
    private String address;
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
    @ManyToOne(optional = false, fetch = FetchType.EAGER, targetEntity = CustomerProfileEntity.class)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfileEntity customerProfile;
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE}, targetEntity = OrderItemEntity.class, mappedBy = "id")
    private Set<OrderItemEntity> orderItems;
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE}, targetEntity = OrderHistoryEntity.class, mappedBy = "id")
    private Set<OrderHistoryEntity> orderHistories;
}
