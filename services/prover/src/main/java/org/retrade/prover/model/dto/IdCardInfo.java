package org.retrade.prover.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdCardInfo {
    private String idNumber;
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String nationality;
    private String placeOfOrigin;
    private String placeOfResidence;
    private String dateOfExpiry;
    private String dateOfIssue;
    private String placeOfIssue;
    private String documentType;
    private String side;
    private String rawApiResponse;
}
