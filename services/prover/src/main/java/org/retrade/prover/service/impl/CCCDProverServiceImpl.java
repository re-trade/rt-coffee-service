package org.retrade.prover.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.prover.service.CCCDProverService;
import org.retrade.prover.service.FileEncryptService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CCCDProverServiceImpl implements CCCDProverService {
    private final FileEncryptService fileEncryptService;
}
