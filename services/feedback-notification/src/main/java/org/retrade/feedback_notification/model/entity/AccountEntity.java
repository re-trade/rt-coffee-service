package org.retrade.feedback_notification.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "accounts")
public class AccountEntity extends BaseSQLEntity {
    @Column(name = "account_id", unique = true, nullable = false)
    private String accountId;
    @Column(name = "username", unique = true, nullable = false)
    private String username;
}
