package org.retrade.feedback_notification.cron;

import lombok.RequiredArgsConstructor;
import org.retrade.feedback_notification.client.TokenServiceClient;
import org.retrade.feedback_notification.repository.AccountRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountUpdateCronJob {
    private final TokenServiceClient tokenServiceClient;
    private final AccountRepository accountRepository;

    @Scheduled(cron = "0 0 0,12 * * ?")
    public void updateAccounts() {
        accountRepository.findAll().forEach(account -> {
            var grpcResponse = tokenServiceClient.getAccountInfoById(account.getAccountId());
            if (!grpcResponse.getIsValid()) {
                return;
            }
            var userInfo = grpcResponse.getUserInfo();
            if (userInfo.getChangedUsername()) {
                account.setUsername(userInfo.getUsername());
                accountRepository.save(account);
            }
        });
    }
}
