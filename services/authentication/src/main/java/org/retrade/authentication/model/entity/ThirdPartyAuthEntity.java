package org.retrade.authentication.model.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Data
@Builder
public class ThirdPartyAuthEntity {
    @Field(value = "provider", targetType = FieldType.STRING)
    private String provider;
    @Field(value = "provider_id", targetType = FieldType.STRING)
    private String providerId;
    @Field(value = "email_provider", targetType = FieldType.STRING)
    private String providerEmail;
}
