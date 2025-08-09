package org.retrade.feedback_notification.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.feedback_notification.client.TokenServiceClient;
import org.retrade.feedback_notification.model.entity.AccountEntity;
import org.retrade.feedback_notification.repository.AccountRepository;
import org.retrade.feedback_notification.util.AuthUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final TokenServiceClient tokenServiceClient;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var account = accountRepository.findByUsername(username).orElseGet(() -> {
            var grpcResponse = tokenServiceClient.getAccountInfoByUsername(username);
            if (!grpcResponse.getIsValid()) {
                throw new UsernameNotFoundException("User not found");
            }
            var userInfo = grpcResponse.getUserInfo();
            if (userInfo.getChangedUsername()) {
                var accountEntity = accountRepository.findByAccountId(userInfo.getAccountId()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
                accountEntity.setUsername(username);
                return accountRepository.save(accountEntity);
            }
            var accountSave = AccountEntity.builder()
                    .username(username)
                    .accountId(userInfo.getAccountId())
                    .roles(new HashSet<>(userInfo.getRolesList()))
                    .build();
            return accountRepository.save(accountSave);
        });
        return User.builder()
                .username(account.getUsername())
                .authorities(AuthUtils.convertRoleToAuthority(account))
                .build();
    }
}
