package org.retrade.feedback_notification.model.entity;

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
@Entity(name = "notifications")
public class NotificationEntity extends BaseSQLEntity {
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = AccountEntity.class)
    @JoinColumn(name = "account_id")
    private AccountEntity account;
    @Column(name = "title")
    private String title;
    @Column(name = "message", length = 256)
    private String message;
    @Column(name = "type")
    private String type;
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    @Column(name = "read", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean read;
}
