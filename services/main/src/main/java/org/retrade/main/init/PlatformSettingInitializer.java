package org.retrade.main.init;

import lombok.RequiredArgsConstructor;
import org.retrade.main.model.entity.PlatformFeeTierEntity;
import org.retrade.main.repository.jpa.PlatformFeeTierRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PlatformSettingInitializer implements CommandLineRunner {
    private final PlatformFeeTierRepository platformFeeTierEntityRepository;
    @Override
    public void run(String... args) throws Exception {
        if (platformFeeTierEntityRepository.count() == 0) {
            List<PlatformFeeTierEntity> defaultTiers = List.of(
                    PlatformFeeTierEntity.builder()
                            .minPrice(BigDecimal.ZERO)
                            .maxPrice(BigDecimal.valueOf(500_000))
                            .feeRate(BigDecimal.valueOf(0.05))
                            .description("Fee 5% for orders < 500,000")
                            .build(),
                    PlatformFeeTierEntity.builder()
                            .minPrice(BigDecimal.valueOf(500_000))
                            .maxPrice(BigDecimal.valueOf(1_000_000))
                            .feeRate(BigDecimal.valueOf(0.04))
                            .description("Fee 4% for orders between 500,000 and 1,000,000")
                            .build(),
                    PlatformFeeTierEntity.builder()
                            .minPrice(BigDecimal.valueOf(1_000_000))
                            .maxPrice(null)
                            .feeRate(BigDecimal.valueOf(0.03))
                            .description("Fee 3% for orders > 1,000,000")
                            .build()
            );
            platformFeeTierEntityRepository.saveAll(defaultTiers);
            System.out.println("✅ Default platform fee tiers have been initialized.");
        } else {
            System.out.println("ℹ️ Platform fee tiers already exist, skipping initialization.");
        }
    }
}
