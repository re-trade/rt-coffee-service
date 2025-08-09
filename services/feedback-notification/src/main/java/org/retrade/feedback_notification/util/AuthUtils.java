package org.retrade.feedback_notification.util;

import lombok.RequiredArgsConstructor;
import org.retrade.feedback_notification.model.entity.AccountEntity;
import org.retrade.feedback_notification.repository.AccountRepository;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthUtils {
    private final AccountRepository accountRepository;
    public AccountEntity getUserAccountFromAuthentication() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) { throw new AuthenticationException("Authentication required") {}; }
            String username = auth.getName();
            return accountRepository.findByUsername(username).orElseThrow();
        } catch (Exception ex) {
            throw new AuthenticationException("This user isn't authentication, please login again") {};
        }
    }
    public Optional<AccountEntity> getCurrentUserAccount() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return Optional.empty();
            String username = auth.getName();
            return accountRepository.findByUsername(username);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public Set<String> getRolesFromAuthUser() {
        var account = getUserAccountFromAuthentication();
        return new HashSet<>(convertAccountToRole(account));
    }

    public static Collection<GrantedAuthority> convertRoleToAuthority (AccountEntity account) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        account.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role));
        });
        return authorities;
    }

    public static List<String> convertAccountToRole (AccountEntity account) {
        return convertRoleToAuthority(account).stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
    }
}
