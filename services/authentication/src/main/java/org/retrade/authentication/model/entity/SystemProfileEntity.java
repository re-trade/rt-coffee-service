package org.retrade.authentication.model.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Data
public class SystemProfileEntity {
    @Field(value = "full_name", targetType = FieldType.STRING)
    private String fullName;
}
