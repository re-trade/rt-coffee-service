package org.retrade.prover.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FptAiData {
    private String id;
    private String name;
    private String dob;
    private String sex;
    private String placeOfOrigin;
    private String placeOfResidence;
    private String doe;
    private String doi;
    private String poi;
    private String type;
    private String cardSide;
}
