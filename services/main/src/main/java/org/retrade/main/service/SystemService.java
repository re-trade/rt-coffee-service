package org.retrade.main.service;

import org.retrade.main.model.dto.response.AccountResponse;
import org.retrade.main.model.dto.response.SellerBaseResponse;

public interface SystemService {
    void approveSeller(String sellerId);
}
