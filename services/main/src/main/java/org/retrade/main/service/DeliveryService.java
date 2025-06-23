package org.retrade.main.service;

import org.retrade.main.model.dto.request.DeliveryTrackRequest;
import org.retrade.main.model.dto.response.DeliveryResponse;

public interface DeliveryService {
    DeliveryResponse signDelivery(DeliveryTrackRequest request);
}
