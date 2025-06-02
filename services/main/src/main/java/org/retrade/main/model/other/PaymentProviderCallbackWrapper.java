package org.retrade.main.model.other;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentProviderCallbackWrapper {
    private String callbackUrl;
    private Boolean success;
    private String message;
}
