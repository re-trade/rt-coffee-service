package org.retrade.main.model.entity;

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
@Entity(name = "customer_contacts")
public class CustomerContactEntity extends BaseSQLEntity {
    @Column(name = "customer_name", length = 50, nullable = false)
    private String customerName;
    @Column(name = "phone", length = 12, nullable = false)
    private String phone;
    @Column(name = "state", length = 20, nullable = false)
    private String state;
    @Column(name = "country", length = 20, nullable = false)
    private String country;
    @Column(name = "district", length = 20, nullable = false)
    private String district;
    @Column(name = "ward", length = 20, nullable = false)
    private String ward;
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
