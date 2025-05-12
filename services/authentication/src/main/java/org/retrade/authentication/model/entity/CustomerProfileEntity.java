package org.retrade.authentication.model.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Data
@Builder
public class CustomerProfileEntity {
    @Field(value = "first_name", targetType = FieldType.STRING)
    private String firstName;
    @Field(value = "last_name", targetType = FieldType.STRING)
    private String lastName;
    @Field(value = "phone", targetType = FieldType.STRING)
    private String phone;
    @Field(value = "address", targetType = FieldType.STRING)
    private String address;
    @Field(value = "avatar_url")
    private String avatarUrl;
}
