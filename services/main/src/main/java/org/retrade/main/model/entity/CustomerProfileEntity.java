package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "customer_profiles")
public class CustomerProfileEntity extends BaseSQLEntity {
    @Column(name = "first_name", length = 255)
    private String firstName;
    @Column(name = "last_name", length = 255)
    private String lastName;
    @Column(name = "phone", length = 20)
    private String phone;
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;
    @OneToOne(fetch = FetchType.EAGER, optional = false, targetEntity = AccountEntity.class)
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    private AccountEntity account;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<CustomerContactEntity> contacts;
}
