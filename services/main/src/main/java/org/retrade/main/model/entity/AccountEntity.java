package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "accounts")
public class AccountEntity extends BaseSQLEntity {
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    @Column(name = "hash_password", nullable = false)
    private String hashPassword;
    @Column(name = "secret", nullable = false, unique = true)
    private String secret;
    @Column(name = "enabled", nullable = false)
    private boolean enabled;
    @Column(name = "locked", nullable = false)
    private boolean locked;
    @Column(name = "two_fa", nullable = false)
    private boolean using2FA;
    @Column(name = "join_in_date", nullable = false)
    private LocalDateTime joinInDate;
    @Column(name = "changed_username", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean changedUsername;
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    @Column(name = "balance", nullable = false, columnDefinition = "DECIMAL(19,4) DEFAULT 0.00")
    private BigDecimal balance;
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, mappedBy = "account")
    private CustomerEntity customer;
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, mappedBy = "account")
    private SellerEntity seller;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = {CascadeType.PERSIST}, mappedBy = "id")
    private Set<LoginSessionEntity> loginSessions;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = {CascadeType.PERSIST}, mappedBy = "id")
    private Set<ThirdPartyAuthEntity> thirdPartyAuths;
    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "account")
    private Set<AccountRoleEntity> accountRoles;
}
