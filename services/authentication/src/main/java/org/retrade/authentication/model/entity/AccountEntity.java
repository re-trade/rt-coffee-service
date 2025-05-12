package org.retrade.authentication.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.retrade.authentication.model.constant.AuthType;
import org.retrade.common.model.entity.BaseMongoEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Document(collection = "customer_accounts")
public class AccountEntity extends BaseMongoEntity {
    @Field(value = "username", targetType = FieldType.STRING)
    @Indexed(unique = true)
    private String username;
    @Field(value = "email", targetType = FieldType.STRING)
    @Indexed(unique = true)
    private String email;
    @Field(value = "auth_type", targetType = FieldType.STRING)
    private AuthType authType;
    @Field(value = "hash_password", targetType = FieldType.STRING)
    private String hashPassword;
    @Field(value = "secret", targetType = FieldType.STRING)
    private String secret;
    @Field(value = "enabled", write = Field.Write.NON_NULL)
    private boolean enabled;
    @Field(value = "locked", write = Field.Write.NON_NULL, targetType = FieldType.BOOLEAN)
    private boolean locked;
    @Field(value = "using_2fa", write = Field.Write.NON_NULL, targetType = FieldType.BOOLEAN)
    private boolean using2FA;
    @Field(value = "customer_profile")
    private CustomerProfileEntity customerProfile;
    @Field(value = "partner_profile")
    private PartnerProfileEntity partnerProfile;
    @Field(value = "system_profile")
    private SystemProfileEntity systemProfile;
    @Field(value = "login_sessions")
    private Set<LoginSessionEntity> loginSessions;
    @Field(value = "third_party_auths")
    private Set<ThirdPartyAuthEntity> thirdPartyAuths;
}
