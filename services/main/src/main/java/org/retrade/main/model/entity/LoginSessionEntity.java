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
    @ManyToOne(optional = false, cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, targetEntity = AccountEntity.class)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;
    @Column(name = "device_fingerprint")
    private String deviceFingerprint;
    @Column(name = "device_name")
    private String deviceName;
    @Column(name = "ip_address", length = 15, nullable = false)
    private String ipAddress;
    @Column(name = "location")
    private String location;
    @Column(name = "user_agent")
    private String userAgent;
    @Column(name = "login_time", nullable = false)
    private Timestamp loginTime;
}
