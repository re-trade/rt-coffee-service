package org.retrade.main.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.retrade.base_proto.GrpcTokenServiceGrpc;
import org.retrade.base_proto.TokenRequest;
import org.retrade.base_proto.TokenValidationResponse;
import org.retrade.base_proto.UserTokenResponse;
import org.retrade.main.model.constant.JwtTokenType;
import org.retrade.main.model.other.UserClaims;
import org.retrade.main.service.JwtService;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Collections;
import java.util.Objects;

@GrpcService
@RequiredArgsConstructor
public class TokenGrpcServiceImpl extends GrpcTokenServiceGrpc.GrpcTokenServiceImplBase {
    private final JwtService jwtService;

    @Override
    public void verifyToken(TokenRequest request, StreamObserver<TokenValidationResponse> responseObserver) {
        UserClaims userClaims = null;
        switch (request.getType()) {
            case ACCESS_TOKEN:
                userClaims = jwtService.getUserClaimsFromJwt(request.getToken(), JwtTokenType.ACCESS_TOKEN).orElse(null);
                break;
            case REFRESH_TOKEN:
                userClaims = jwtService.getUserClaimsFromJwt(request.getToken(), JwtTokenType.REFRESH_TOKEN).orElse(null);
                break;
            default:
                responseObserver.onNext(TokenValidationResponse.newBuilder()
                        .setIsValid(false)
                        .setErrorMessage("Token type is's supported")
                        .build());
                responseObserver.onCompleted();
                return;
        }
        TokenValidationResponse tokenRpcResponse = TokenValidationResponse.newBuilder()
                .setIsValid(true)
                .setUserInfo(UserTokenResponse.newBuilder()
                        .addAllRoles(Objects.requireNonNullElse(userClaims.getRoles(), Collections.emptyList()))
                        .setUsername(userClaims.getUsername())
                        .setIsActive(true)
                        .setIsVerified(true)
                        .setType(request.getType())
                        .build())
                .setErrorMessage("")
                .build();
        responseObserver.onNext(tokenRpcResponse);
    }
}
