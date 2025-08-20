package org.retrade.prover.model.other;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IdentityCardOCRWrapper {
    private String identityNumber;
    private String fullName;
    private String dateOfBirth;
    private String sex;
    private String nationality;
    private String placeOfOrigin;
    private String placeOfResidence;
    private String dateOfExpiry;
}
