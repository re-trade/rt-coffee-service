package org.retrade.main.model.other;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;

@Data
public class GoogleProfileResponse implements Serializable {
    private String sub;
    private String name;
    @SerializedName("given_name")
    private String givenName;
    @SerializedName("family_name")
    private String familyName;
    private String picture;
    private String email;
    @SerializedName("email_verified")
    private boolean emailVerified;
}
