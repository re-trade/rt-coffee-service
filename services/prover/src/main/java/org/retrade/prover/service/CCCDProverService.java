package org.retrade.prover.service;

import org.retrade.prover.model.message.CCCDVerificationMessage;

public interface CCCDProverService {
    void processVerification(CCCDVerificationMessage message);
}
