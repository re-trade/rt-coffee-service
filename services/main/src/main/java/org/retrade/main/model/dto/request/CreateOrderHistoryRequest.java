package org.retrade.main.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.constant.DeliveryTypeEnum;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderHistoryRequest {
    private String orderComboId;
    private String notes;
    private String newStatusId;
    private String deliveryCode;
    private DeliveryTypeEnum deliveryType;
    private Set<String> deliveryEvidenceImages;
}
