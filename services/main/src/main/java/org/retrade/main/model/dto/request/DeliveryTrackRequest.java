package org.retrade.main.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.constant.DeliveryTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryTrackRequest {
    private String orderComboId;
    private String deliveryCode;
    private DeliveryTypeEnum deliveryType;
}
