package org.retrade.main.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.main.service.PaymentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
}
