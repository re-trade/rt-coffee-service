package org.retrade.main.cron;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.main.model.constant.IdentityVerifiedStatusEnum;
import org.retrade.main.repository.jpa.SellerRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SellerProfileCronjob {
    private final SellerRepository sellerRepository;
    @Scheduled(cron = "0 * * * * *")
    public void syncSellerProfile() {
        log.info("Starting seller profile synchronization...");
        LocalDateTime expiredBefore = LocalDateTime.now().minusMinutes(15);
        var result = sellerRepository.deleteByIdentityVerifiedAndNotSellerAndExpired(IdentityVerifiedStatusEnum.INIT, expiredBefore);
        log.info("Deleted {} sellers with IdentityVerifiedStatusEnum.INIT", result);
    }
}
