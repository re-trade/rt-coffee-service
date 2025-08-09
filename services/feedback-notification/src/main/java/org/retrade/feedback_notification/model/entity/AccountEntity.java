package org.retrade.feedback_notification.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "accounts")
public class AccountEntity extends BaseSQLEntity {
    @Column(name = "account_id", unique = true, nullable = false)
    private String accountId;
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    @Column(name = "roles", nullable = false)
    private Set<String> roles;
}
