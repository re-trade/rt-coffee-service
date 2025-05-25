package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.main.repository.PaymentHistoryRepository;
import org.retrade.main.repository.PaymentMethodRepository;
import org.retrade.main.service.PaymentService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
}
