package org.retrade.main.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawApproveRequest {
    private String withdrawId;
    private Boolean approved;
    private String imageReview;
    private String rejectReason;
}
