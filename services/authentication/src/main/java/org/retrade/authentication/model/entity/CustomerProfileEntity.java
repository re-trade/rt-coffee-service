package org.retrade.authentication.model.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Data
@Builder
public class CustomerProfileEntity {
    @Field(name = "first_name", targetType = FieldType.STRING)
    private String firstName;
    @Field(name = "last_name", targetType = FieldType.STRING)
    private String lastName;
    @Field(name = "phone", targetType = FieldType.STRING)
    private String phone;
    @Field(name = "address", targetType = FieldType.STRING)
    private String address;
    @Field(name = "avatar_url")
    private String avatarUrl;
}
