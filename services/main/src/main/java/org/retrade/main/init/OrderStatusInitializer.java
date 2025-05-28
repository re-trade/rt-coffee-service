package org.retrade.main.init;

import lombok.RequiredArgsConstructor;
import org.retrade.main.model.entity.OrderStatusEntity;
import org.retrade.main.repository.OrderStatusRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderStatusInitializer implements CommandLineRunner {
    private final OrderStatusRepository orderStatusRepository;
    @Override
    public void run(String... args) throws Exception {
        List<OrderStatusEntity> defaultOrderStatuses = List.of(
                OrderStatusEntity.builder()
                        .name("Pending")
                        .code("PENDING")
                        .enabled(true)
                        .build(),

                OrderStatusEntity.builder()
                        .name("Payment Confirmation")
                        .code("PAYMENT_CONFIRMATION")
                        .enabled(true)
                        .build(),

                OrderStatusEntity.builder()
                        .name("Preparing")
                        .code("PREPARING")
                        .enabled(true)
                        .build(),

                OrderStatusEntity.builder()
                        .name("Delivering")
                        .code("DELIVERING")
                        .enabled(true)
                        .build(),

                OrderStatusEntity.builder()
                        .name("Delivered")
                        .code("DELIVERED")
                        .enabled(true)
                        .build(),

                OrderStatusEntity.builder()
                        .name("Cancelled")
                        .code("CANCELLED")
                        .enabled(true)
                        .build(),

                OrderStatusEntity.builder()
                        .name("Payment Cancelled")
                        .code("PAYMENT_CANCELLED")
                        .enabled(true)
                        .build()
        );
        defaultOrderStatuses.forEach(orderStatus -> {
            orderStatusRepository.findByCode(orderStatus.getCode())
                    .ifPresentOrElse(
                            existing -> System.out.println("Order status already exists: " + existing.getCode()),
                            () -> {
                                orderStatusRepository.save(orderStatus);
                                System.out.println("Added order status: " + orderStatus.getCode());
                            }
                    );
        });
    }
}
