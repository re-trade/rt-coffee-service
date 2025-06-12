package org.retrade.prover.service;

import org.retrade.prover.model.message.CCCDVerificationMessage;
import org.retrade.prover.model.other.CCCDValidateWrapper;

public interface CCCDProverService {
    CCCDValidateWrapper processVerification(CCCDVerificationMessage message);
}
