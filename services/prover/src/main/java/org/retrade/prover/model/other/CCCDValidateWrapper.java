package org.retrade.prover.model.other;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CCCDValidateWrapper {
    private String message;
    private Boolean valid;
}
