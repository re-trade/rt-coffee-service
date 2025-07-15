package org.retrade.migration.constant;

import lombok.Getter;

@Getter
public enum ExchangeNameEnum {
    NOTIFICATION_EXCHANGE("notification.exchange"),
    NOTIFICATION_RETRY_EXCHANGE("notification.dlx.exchange"),
    REGISTRATION_EXCHANGE("registration.exchange"),
    REGISTRATION_RETRY_EXCHANGE("registration.dlx.exchange"),
    IDENTITY_EXCHANGE("identity.exchange"),
    IDENTITY_RETRY_EXCHANGE("identity.dlx.exchange");
    private final String name;
    ExchangeNameEnum(String name) {
        this.name = name;
    }
}
