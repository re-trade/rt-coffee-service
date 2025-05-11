package org.retrade.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.retrade.authentication.model.dto.request.CustomerAccountRegisterRequest;
import org.retrade.authentication.repository.AccountRepository;
import org.retrade.authentication.service.RegisterService;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {
    private final AccountRepository accountRepository;

    public void customerRegister (CustomerAccountRegisterRequest request) {
        var result = accountRepository.findById(request.getUsername())
                .orElseThrow(() -> new RuntimeException(""));
    }
}
