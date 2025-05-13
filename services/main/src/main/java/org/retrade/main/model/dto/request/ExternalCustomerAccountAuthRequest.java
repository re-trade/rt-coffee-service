package org.retrade.main.model.dto.request;

import lombok.Builder;
import lombok.Data;
import org.retrade.main.model.constant.AuthType;

@Data
@Builder
public class ExternalCustomerAccountAuthRequest {
    private String uid;
    private AuthType authType;
    private String token;
}
