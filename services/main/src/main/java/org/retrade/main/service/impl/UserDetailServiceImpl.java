package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.main.repository.AccountRepository;
import org.retrade.main.util.AuthUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {
    private final AccountRepository accountRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null) {
            throw new UsernameNotFoundException("Username cannot be null");
        }
        var account = accountRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return User.builder()
                .username(account.getUsername())
                .password(account.getHashPassword())
                .authorities(AuthUtils.convertRoleToAuthority(account))
                .disabled(!account.isEnabled())
                .accountLocked(account.isLocked())
                .build();
    }
    
}
