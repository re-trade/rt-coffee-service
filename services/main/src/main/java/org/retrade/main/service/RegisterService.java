package org.retrade.main.service;

import org.retrade.main.model.dto.request.CustomerAccountRegisterRequest;
import org.retrade.main.model.dto.response.CustomerAccountRegisterResponse;

public interface RegisterService {
    CustomerAccountRegisterResponse customerRegister (CustomerAccountRegisterRequest request);
}
