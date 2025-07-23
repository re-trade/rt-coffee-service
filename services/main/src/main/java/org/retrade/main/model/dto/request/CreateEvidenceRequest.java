package org.retrade.main.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEvidenceRequest {
    private Set<String> evidenceUrls;
    private String note;
}
