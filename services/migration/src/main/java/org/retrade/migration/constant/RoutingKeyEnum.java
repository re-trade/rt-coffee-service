package org.retrade.migration.constant;

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
    IDENTITY_RETRY_ROUTING_KEY("identity.retry"),
    ACHIEVEMENT_ROUTING_KEY("achievement.seller.event.routing-key"),
    ACHIEVEMENT_RETRY_ROUTING_KEY("achievement.seller.event.retry.routing-key"),
    SOCKET_NOTIFICATION_ROUTING_KEY("socket.notification"),
    SOCKET_RETRY_ROUTING_KEY("socket.retry");
    private final String name;
    RoutingKeyEnum(String name) {
        this.name = name;
    }
}
