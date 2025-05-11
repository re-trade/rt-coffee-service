package org.retrade.authentication.model.entity;

import org.springframework.data.mongodb.core.mapping.Field;

public class PartnerProfileEntity {
    @Field(name = "full_name")
    private String fullName;
    @Field(name = "phone_number")
    private String phoneNumber;
    @Field(name = "avatar_url")
    private String avatarUrl;
    @Field(name = "business_name")
    private String businessName;
    @Field(name = "business_license")
    private String businessLicense;
}
