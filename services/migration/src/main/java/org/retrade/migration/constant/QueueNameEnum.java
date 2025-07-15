package org.retrade.migration.constant;

import lombok.Getter;

@Getter
public enum QueueNameEnum {
    EMAIL_NOTIFICATION_QUEUE("email.notification.queue"),
    EMAIL_RETRY_QUEUE("email.dlx.queue"),
    USER_REGISTRATION_QUEUE("user.registration.queue"),
    USER_REGISTRATION_RETRY_QUEUE("user.registration.dlx.queue"),
    DEAD_LETTER_QUEUE("dead.letter.queue"),
    IDENTITY_VERIFICATION_QUEUE("identity.verification.queue"),
    IDENTITY_RETRY_QUEUE("identity.dlx.queue"),
    IDENTITY_VERIFIED_RESULT_QUEUE("identity.verified.result.queue"),
    IDENTITY_VERIFIED_RETRY_QUEUE("identity.verified.retry.queue");
    private final String name;
    QueueNameEnum(String name) {
        this.name = name;
    }
}
