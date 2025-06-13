package org.retrade.prover.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FptAiData {
    private String id;
    private String name;
    private String dob;
    private String sex;
    private String nationality;
    private String home;
    private String address;
    private String doe;
    private String type;
    // Back
    private String features;
    @JsonProperty("issue_date")
    private String issueDate;
    @JsonProperty("issue_loc")
    private String issueLoc;
    private String pob;
    @JsonProperty("mrz_details")
    private MRZDetail mrzDetail;

}
