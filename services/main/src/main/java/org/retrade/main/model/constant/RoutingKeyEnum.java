package org.retrade.main.model.constant;

import lombok.Getter;

@Getter
public enum RoutingKeyEnum {
    EMAIL_NOTIFICATION_ROUTING_KEY("email.notification"),
    EMAIL_RETRY_ROUTING_KEY("email.retry"),
    USER_REGISTRATION_ROUTING_KEY("user.registration"),
    USER_REGISTRATION_RETRY_ROUTING_KEY("user.registration.retry"),
    DEAD_LETTER_ROUTING_KEY("dead.letter"),
    IDENTITY_VERIFICATION_ROUTING_KEY("identity.verification"),
    IDENTITY_VERIFIED_ROUTING_KEY("identity.verified"),
    IDENTITY_RETRY_ROUTING_KEY("identity.retry");
    private final String name;
    RoutingKeyEnum(String name) {
        this.name = name;
    }
}
