package org.retrade.main.cron;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.retrade.main.repository.jpa.ProductRepository;
import org.retrade.main.repository.jpa.SellerRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ProductReviewCronjob {
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private LocalDateTime lastSyncTime = LocalDateTime.now().minusDays(1);

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void syncUpdatedRatings() {
        try {
            productRepository.updateProductAverageRatings(lastSyncTime);
            sellerRepository.updateSellerAverageRatings(lastSyncTime);
            lastSyncTime = LocalDateTime.now();
            System.out.println("Rating synchronization completed at " + lastSyncTime);
        } catch (Exception e) {
            System.out.println("Error during rating synchronization: " + e.getMessage());
        }
    }
}
