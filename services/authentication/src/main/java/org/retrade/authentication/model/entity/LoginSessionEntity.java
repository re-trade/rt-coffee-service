package org.retrade.authentication.model.entity;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.sql.Timestamp;

public class LoginSessionEntity {
    @Field(value = "ip", targetType = FieldType.STRING)
    private String ip;
    @Field(value = "login_time", targetType = FieldType.TIMESTAMP)
    private Timestamp loginTime;
}
