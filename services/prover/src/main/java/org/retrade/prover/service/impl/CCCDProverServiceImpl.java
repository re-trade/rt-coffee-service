package org.retrade.prover.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.prover.model.message.CCCDVerificationMessage;
import org.retrade.prover.service.CCCDProverService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CCCDProverServiceImpl implements CCCDProverService {

    @Override
    public void processVerification(CCCDVerificationMessage message) {

    }
}
