package org.retrade.prover.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MRZDetail {
    private String id;
    private String name;
    private String doe;
    private String dob;
    private String nationality;
    private String sex;
}
