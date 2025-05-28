package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "account_roles")
public class AccountRoleEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;
    @ManyToOne(targetEntity = RoleEntity.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;
}
