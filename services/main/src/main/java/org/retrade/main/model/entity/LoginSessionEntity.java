package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.sql.Timestamp;

@Entity(name = "login_sessions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginSessionEntity extends BaseSQLEntity {
    @Column(name = "ip", length = 15, nullable = false)
    private String ip;
    @Column(name = "login_time", nullable = false)
    private Timestamp loginTime;
    @ManyToOne(optional = false, cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, targetEntity = AccountEntity.class)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;
}
