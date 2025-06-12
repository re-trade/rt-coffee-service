package org.retrade.prover.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FptAiApiResponse {
    private int errorCode;
    private String errorMessage;
    private FptAiData data;
}
