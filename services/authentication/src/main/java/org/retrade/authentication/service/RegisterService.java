package org.retrade.authentication.service;

import org.retrade.authentication.model.dto.request.CustomerAccountRegisterRequest;
import org.retrade.authentication.model.dto.response.CustomerAccountRegisterResponse;

public interface RegisterService {
    CustomerAccountRegisterResponse customerRegister (CustomerAccountRegisterRequest request);
}
