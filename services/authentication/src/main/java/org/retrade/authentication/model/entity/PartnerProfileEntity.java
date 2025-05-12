package org.retrade.authentication.model.entity;

import org.springframework.data.mongodb.core.mapping.Field;

public class PartnerProfileEntity {
    @Field(value = "full_name")
    private String fullName;
    @Field(value = "phone_number")
    private String phoneNumber;
    @Field(value = "avatar_url")
    private String avatarUrl;
    @Field(value = "business_name")
    private String businessName;
    @Field(value = "business_license")
    private String businessLicense;
}
