package org.retrade.authentication.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.retrade.authentication.model.constant.AuthType;
import org.retrade.common.model.entity.BaseMongoEntity;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Document(collection = "customer_accounts")
public class AccountEntity extends BaseMongoEntity {
    @Field(name = "username", targetType = FieldType.STRING)
    @Indexed(unique = true)
    private String username;
    @Field(name = "email", targetType = FieldType.STRING)
    @Indexed(unique = true)
    private String email;
    @Field(name = "auth_type", targetType = FieldType.STRING)
    private AuthType authType;
    @Field(name = "hash_password", targetType = FieldType.STRING)
    private String hashPassword;
    @Field(name = "secret", targetType = FieldType.STRING)
    private String secret;
    @Field(name = "enabled", value = "true", write = Field.Write.NON_NULL)
    private boolean enabled;
    @Field(name = "locked", value = "false", write = Field.Write.NON_NULL, targetType = FieldType.BOOLEAN)
    private boolean locked;
    @Field(name = "using_2fa", value = "false", write = Field.Write.NON_NULL, targetType = FieldType.BOOLEAN)
    private boolean using2FA;
    @Field(name = "customer_profile")
    private CustomerProfileEntity customerProfile;
    @Field(name = "partner_profile")
    private PartnerProfileEntity partnerProfile;
    @Field(name = "system_profile")
    private SystemProfileEntity systemProfile;
}
