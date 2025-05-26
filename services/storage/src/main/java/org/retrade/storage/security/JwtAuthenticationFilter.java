package org.retrade.storage.security;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.proto.authentication.GrpcTokenServiceGrpc;
import org.retrade.proto.authentication.TokenRequest;
import org.retrade.proto.authentication.TokenType;
import org.retrade.storage.util.TokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Value("${grpc.client.main-service.host:localhost}")
    private String mainServiceHost;

    @Value("${grpc.client.main-service.port:9080}")
    private int mainServicePort;
    private GrpcTokenServiceGrpc.GrpcTokenServiceBlockingStub blockingStub;
    private ManagedChannel channel;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain filterChain) throws ServletException, IOException {
        String accessToken = TokenUtils.getTokenFromHeader(request);
        if (accessToken != null) {
            var userClaims = blockingStub.verifyToken(TokenRequest.newBuilder()
                            .setToken(accessToken)
                            .setType(TokenType.ACCESS_TOKEN)
                    .build());
            if (userClaims.getIsValid()) {
                var claims = userClaims.getUserInfo();
                var roles = claims.getRolesList().stream().map(SimpleGrantedAuthority::new).toList();
                UserDetails userDetails = User.builder()
                        .username(claims.getUsername())
                        .password("")
                        .authorities(roles)
                        .disabled(!claims.getIsActive())
                        .accountLocked(!claims.getIsVerified())
                        .build();
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }


    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(mainServiceHost, mainServicePort)
                .usePlaintext()
                .build();
        blockingStub = GrpcTokenServiceGrpc.newBlockingStub(channel);
        log.info("Main gRPC client initialized for {}:{}", mainServiceHost, mainServicePort);
    }

    @PreDestroy
    public void destroy() {
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                log.info("Main gRPC client channel closed");
            } catch (InterruptedException e) {
                log.warn("Failed to close gRPC channel gracefully", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
