package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "customer_contacts")
public class CustomerContactEntity extends BaseSQLEntity {
    @Column(name = "customer_name", length = 50, nullable = false)
    private String customerName;
    @Column(name = "phone", length = 12, nullable = false)
    private String phone;
    @Column(name = "state", length = 50, nullable = false)
    private String state;
    @Column(name = "country", length = 50, nullable = false)
    private String country;
    @Column(name = "district", length = 50, nullable = false)
    private String district;
    @Column(name = "ward", length = 50, nullable = false)
    private String ward;
    @Column(name = "address_line", nullable = false)
    private String addressLine;
    @Column(name = "name", length = 50, nullable = false)
    private String name;
    @Column(name = "defaulted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean defaulted;
    @Column(name = "type", nullable = false)
    private Integer type;
    @ManyToOne(targetEntity = CustomerEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;
}
