package org.retrade.authentication.model.dto.request;

import lombok.Builder;
import lombok.Data;
import org.retrade.authentication.model.constant.AuthType;

@Data
@Builder
public class ExternalCustomerAccountAuthRequest {
    private String uid;
    private AuthType authType;
    private String token;
}
