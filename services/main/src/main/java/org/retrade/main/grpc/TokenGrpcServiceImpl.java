package org.retrade.main.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.retrade.main.model.constant.JwtTokenType;
import org.retrade.main.model.other.UserClaims;
import org.retrade.main.repository.AccountRepository;
import org.retrade.main.service.JwtService;
import org.retrade.proto.authentication.*;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

@GrpcService
@RequiredArgsConstructor
public class TokenGrpcServiceImpl extends GrpcTokenServiceGrpc.GrpcTokenServiceImplBase {
    private final JwtService jwtService;
    private final AccountRepository accountRepository;

    @Override
    public void verifyToken(TokenRequest request, StreamObserver<VerifyTokenResponse> responseObserver) {
        Optional<UserClaims> userClaims = getUserClaimsFromJwt(request.getToken(), request.getType());
        if (userClaims.isEmpty()) {
            responseObserver.onNext(VerifyTokenResponse
                    .newBuilder()
                    .setIsValid(false)
                    .addErrorMessages("Unsupported token type")
                    .build());
            responseObserver.onCompleted();
            return;
        }
        var result = userClaims.get();
        var account = accountRepository.findByUsername(result.getUsername());
        if (account.isEmpty()) {
            responseObserver.onNext(VerifyTokenResponse
                    .newBuilder()
                    .setIsValid(false)
                    .addErrorMessages("Account does not exist")
                    .build());
            responseObserver.onCompleted();
            return;
        }
        VerifyTokenResponse tokenRpcResponse = VerifyTokenResponse.newBuilder()
                .setIsValid(true)
                .setUserInfo(UserTokenInfo.newBuilder()
                        .setAcountId(account.get().getId())
                        .addAllRoles(Objects.requireNonNullElse(result.getRoles(), Collections.emptyList()))
                        .setUsername(result.getUsername())
                        .setIsActive(account.get().isEnabled())
                        .setIsVerified(!account.get().isLocked())
                        .setIsVerified(true)
                        .setType(request.getType())
                        .build())
                .addErrorMessages("")
                .build();
        responseObserver.onNext(tokenRpcResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getCustomerProfile(TokenRequest request, StreamObserver<GetCustomerProfileResponse> responseObserver) {
        Optional<UserClaims> userClaims = getUserClaimsFromJwt(request.getToken(), request.getType());
        if (userClaims.isEmpty()) {
            responseObserver.onNext(GetCustomerProfileResponse.newBuilder()
                    .setIsValid(false)
                    .addErrorMessages("Token type is's supported")
                    .build());
            responseObserver.onCompleted();
            return;
        }
        var result = userClaims.get();
        var account = accountRepository.findByUsername(result.getUsername());
        if (account.isEmpty()) {
            responseObserver.onNext(GetCustomerProfileResponse
                    .newBuilder()
                    .setIsValid(false)
                    .addErrorMessages("Account does not exist")
                    .build());
            responseObserver.onCompleted();
            return;
        }
        var customer = account.get().getCustomerProfile();
        if (customer == null) {
            responseObserver.onNext(GetCustomerProfileResponse
                    .newBuilder()
                    .setIsValid(false)
                    .addErrorMessages("Customer does not exist")
                    .build());
            responseObserver.onCompleted();
            return;
        }
        GetCustomerProfileResponse tokenRpcResponse = GetCustomerProfileResponse.newBuilder()
                .setIsValid(true)
                .setUserInfo(CustomerDetailInfo.newBuilder()
                        .addAllRoles(Objects.requireNonNullElse(result.getRoles(), Collections.emptyList()))
                        .setUsername(result.getUsername())
                        .setEmail(account.get().getEmail())
                        .setIsActive(account.get().isEnabled())
                        .setIsVerified(!account.get().isLocked())
                        .setFirstName(customer.getFirstName())
                        .setLastName(customer.getLastName())
                        .setPhone(customer.getPhone())
                        .setAddress(customer.getAddress())
                        .setAccountId(account.get().getId())
                        .setCustomerId(customer.getId())
                        .build())
                .addErrorMessages("")
                .build();
        responseObserver.onNext(tokenRpcResponse);
        responseObserver.onCompleted();
    }

    private Optional<UserClaims> getUserClaimsFromJwt(String token, TokenType tokenType) {
        JwtTokenType type = null;
        switch (tokenType) {
            case ACCESS_TOKEN:
                type = JwtTokenType.ACCESS_TOKEN;
                break;
            case REFRESH_TOKEN:
                type = JwtTokenType.REFRESH_TOKEN;
                break;
            case TWO_FA_TOKEN:
                type = JwtTokenType.TWO_FA_TOKEN;
                break;
            case TOKEN_TYPE_UNSPECIFIED:
                return Optional.empty();
        }
        return jwtService.getUserClaimsFromJwt(token, type);
    }
}
