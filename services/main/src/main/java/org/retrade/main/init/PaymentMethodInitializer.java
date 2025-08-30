package org.retrade.main.init;

import lombok.RequiredArgsConstructor;
import org.retrade.main.model.entity.PaymentMethodEntity;
import org.retrade.main.repository.jpa.PaymentMethodRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentMethodInitializer implements CommandLineRunner {
    private final PaymentMethodRepository paymentMethodRepository;
    @Override
    public void run(String... args) throws Exception {
        List<PaymentMethodEntity> defaultPaymentMethods = List.of(
                PaymentMethodEntity.builder()
                        .name("Pay OS")
                        .description("Pay via Pay OS")
                        .imgUrl("https://payos.vn/wp-content/uploads/2025/05/payos-logo-v2.svg")
                        .code("PAY_OS")
                        .type("3TH_SERVICE")
                        .handlerClass("org.retrade.main.handler.PayOSPaymentHandler")
                        .callbackUri("/payments/callback/payos")
                        .enabled(true)
                        .build(),

                PaymentMethodEntity.builder()
                        .name("VN Pay")
                        .description("Pay via VnPay")
                        .imgUrl("https://stcd02206177151.cloud.edgevnpay.vn/assets/images/logo-icon/logo-primary.svg")
                        .code("VN_PAY")
                        .type("3TH_SERVICE")
                        .handlerClass("org.retrade.main.handler.VNPayPaymentHandler")
                        .callbackUri("/payments/callback/vnp")
                        .enabled(true)
                        .build()
        );
        defaultPaymentMethods.forEach(paymentMethod -> {
            paymentMethodRepository.findByCodeIgnoreCase(paymentMethod.getCode())
                    .ifPresentOrElse(
                            existing -> System.out.println("Payment method already exists: " + existing.getCode()),
                            () -> {
                                paymentMethodRepository.save(paymentMethod);
                                System.out.println("Added payment method: " + paymentMethod.getCode());
                            }
                    );
        });
    }
}
