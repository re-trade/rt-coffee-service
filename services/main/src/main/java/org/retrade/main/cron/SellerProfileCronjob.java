package org.retrade.main.cron;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.main.model.constant.IdentityVerifiedStatusEnum;
import org.retrade.main.repository.jpa.SellerRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SellerProfileCronjob {
    private final SellerRepository sellerRepository;
    @Scheduled(cron = "0 * * * * *")
    @Transactional(rollbackFor = { Exception.class, ActionFailedException.class})
    public void syncSellerProfile() {
        log.info("Starting seller profile synchronization...");
        try {
            LocalDateTime expiredBefore = LocalDateTime.now().minusMinutes(15);
            var result = sellerRepository.deleteByIdentityVerifiedAndNotSellerAndExpired(IdentityVerifiedStatusEnum.INIT, expiredBefore);
            log.info("Deleted {} sellers with IdentityVerifiedStatusEnum.INIT", result);
        } catch (Exception e) {
            log.error("Error during seller profile synchronization: {}", e.getMessage());
            throw new ActionFailedException("Error during seller profile synchronization:", e);
        }
    }
}
