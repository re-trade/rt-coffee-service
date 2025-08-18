package org.retrade.main.cron;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.main.model.constant.ProductStatusEnum;
import org.retrade.main.repository.jpa.ProductRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductStatusCronjob {
    private final ProductRepository productRepository;
    @Scheduled(cron = "0 * * * * *")
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public void syncProductStatus() {
        try {
            var result = productRepository.updateStatusForOutOfStock(ProductStatusEnum.ACTIVE, ProductStatusEnum.INACTIVE);
            log.info("Updated {} products from ACTIVE to INACTIVE", result);
        } catch (Exception e) {
            log.error("Error during product status synchronization: " + e.getMessage());
        }
    }
}
