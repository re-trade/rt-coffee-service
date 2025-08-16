package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.constant.DeliveryTypeEnum;

import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {
    private String orderComboId;
    private String deliveryCode;
    private DeliveryTypeEnum deliveryType;
    private Set<String> deliveryEvidences;
}
