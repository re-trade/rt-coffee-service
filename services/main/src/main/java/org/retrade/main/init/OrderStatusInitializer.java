package org.retrade.main.init;

import lombok.RequiredArgsConstructor;
import org.retrade.main.model.constant.OrderStatusCodes;
import org.retrade.main.model.entity.OrderStatusEntity;
import org.retrade.main.repository.jpa.OrderStatusRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderStatusInitializer implements CommandLineRunner {
    private final OrderStatusRepository orderStatusRepository;
    @Override
    public void run(String... args) {
        Map<String, String> defaultStatuses = Map.ofEntries(
                Map.entry(OrderStatusCodes.PENDING, "Pending"),
                Map.entry(OrderStatusCodes.PAYMENT_CONFIRMATION, "Payment Confirmation"),
                Map.entry(OrderStatusCodes.PREPARING, "Preparing"),
                Map.entry(OrderStatusCodes.DELIVERING, "Delivering"),
                Map.entry(OrderStatusCodes.DELIVERED, "Delivered"),
                Map.entry(OrderStatusCodes.COMPLETED, "Completed"),
                Map.entry(OrderStatusCodes.CANCELLED, "Cancelled"),
                Map.entry(OrderStatusCodes.RETURN_REQUESTED, "Return Requested"),
                Map.entry(OrderStatusCodes.RETURN_APPROVED, "Return Approved"),
                Map.entry(OrderStatusCodes.RETURN_REJECTED, "Return Rejected"),
                Map.entry(OrderStatusCodes.RETURNING, "Returning"),
                Map.entry(OrderStatusCodes.RETURNED, "Returned"),
                Map.entry(OrderStatusCodes.REFUNDED, "Refunded"),
                Map.entry(OrderStatusCodes.RETRIEVED, "Retrieved")
        );

        defaultStatuses.forEach((code, name) -> {
            orderStatusRepository.findByCode(code).ifPresentOrElse(
                    existing -> System.out.println("✔ Status exists: " + code),
                    () -> {
                        OrderStatusEntity status = OrderStatusEntity.builder()
                                .code(code)
                                .name(name)
                                .enabled(true)
                                .build();
                        orderStatusRepository.save(status);
                        System.out.println("➕ Inserted status: " + code);
                    }
            );
        });
    }
}
