package org.retrade.main.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveSellerRequest {
    private String sellerId;
    private Boolean forced;
    private Boolean approve;
}
