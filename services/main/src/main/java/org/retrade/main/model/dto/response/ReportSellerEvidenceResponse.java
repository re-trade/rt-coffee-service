package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.constant.SenderRoleEnum;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSellerEvidenceResponse {
    private String id;
    private SenderRoleEnum senderRole;
    private String senderId;
    private String senderName;
    private String senderAvatarUrl;
    private String notes;
    private Set<String> evidenceUrls;
}
