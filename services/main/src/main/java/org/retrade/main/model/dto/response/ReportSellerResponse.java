package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.OrderComboEntity;
import org.retrade.main.model.entity.SellerEntity;

import java.security.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSellerResponse {
    private SellerEntity seller;
    private String typeReport;
    private String content;
    private String resolutionStatus;
    private String resolutionDetail;
    private Timestamp resolutionDate;
    private OrderComboEntity orderCombo;
    private AccountEntity account;
    private String image;
}
