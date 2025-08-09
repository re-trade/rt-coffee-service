package org.retrade.main.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.retrade.main.model.constant.JwtTokenType;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.CustomerEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.retrade.main.model.other.UserClaims;
import org.retrade.main.repository.jpa.AccountRepository;
import org.retrade.main.repository.jpa.CustomerRepository;
import org.retrade.main.repository.jpa.SellerRepository;
import org.retrade.main.service.JwtService;
import org.retrade.main.util.AuthUtils;
import org.retrade.proto.authentication.*;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@GrpcService
@RequiredArgsConstructor
public class TokenGrpcServiceImpl extends GrpcTokenServiceGrpc.GrpcTokenServiceImplBase {
    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;

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
        var account = unwrapAccountOrRespondVerify(accountRepository.findByUsername(result.getUsername()), responseObserver);
        if (account == null) return;

        VerifyTokenResponse tokenRpcResponse = VerifyTokenResponse.newBuilder()
                .setIsValid(true)
                .setUserInfo(UserTokenInfo.newBuilder()
                        .setAccountId(account.getId())
                        .addAllRoles(Objects.requireNonNullElse(result.getRoles(), Collections.emptyList()))
                        .setUsername(result.getUsername())
                        .setIsActive(account.isEnabled())
                        .setIsVerified(!account.isLocked())
                        .setType(request.getType())
                        .build())
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
                    .addErrorMessages("Unsupported token type")
                    .build());
            responseObserver.onCompleted();
            return;
        }
        var result = userClaims.get();
        var account = unwrapAccountOrRespondCustomer(accountRepository.findByUsername(result.getUsername()), responseObserver);
        if (account == null) return;

        if (!checkCustomerExists(account, responseObserver)) return;

        var customer = account.getCustomer();
        GetCustomerProfileResponse tokenRpcResponse = GetCustomerProfileResponse.newBuilder()
                .setIsValid(true)
                .setUserInfo(CustomerDetailInfo.newBuilder()
                        .addAllRoles(Objects.requireNonNullElse(result.getRoles(), Collections.emptyList()))
                        .setUsername(result.getUsername())
                        .setEmail(account.getEmail())
                        .setIsActive(account.isEnabled())
                        .setIsVerified(!account.isLocked())
                        .setFirstName(customer.getFirstName())
                        .setLastName(customer.getLastName())
                        .setPhone(customer.getPhone())
                        .setAddress(customer.getAddress())
                        .setAccountId(account.getId())
                        .setCustomerId(customer.getId())
                        .setAvatarUrl(customer.getAvatarUrl() != null ? customer.getAvatarUrl() : "")
                        .build())
                .build();
        responseObserver.onNext(tokenRpcResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getSellerProfile(TokenRequest request, StreamObserver<GetSellerProfileResponse> responseObserver) {
        Optional<UserClaims> userClaims = getUserClaimsFromJwt(request.getToken(), request.getType());
        if (userClaims.isEmpty()) {
            responseObserver.onNext(GetSellerProfileResponse.newBuilder()
                    .setIsValid(false)
                    .addErrorMessages("Unsupported token type")
                    .build());
            responseObserver.onCompleted();
            return;
        }
        var result = userClaims.get();
        var account = unwrapAccountOrRespondSeller(accountRepository.findByUsername(result.getUsername()), responseObserver);
        if (account == null) return;

        if (!checkSellerExists(account, responseObserver)) return;

        var seller = account.getSeller();
        GetSellerProfileResponse tokenRpcResponse = GetSellerProfileResponse.newBuilder()
                .setIsValid(true)
                .setUserInfo(SellerDetailInfo.newBuilder()
                        .addAllRoles(Objects.requireNonNullElse(result.getRoles(), Collections.emptyList()))
                        .setUsername(result.getUsername())
                        .setEmail(account.getEmail())
                        .setIsActive(account.isEnabled())
                        .setIsVerified(!account.isLocked())
                        .setSellerName(seller.getShopName())
                        .setAvatarUrl(seller.getAvatarUrl() != null ? seller.getAvatarUrl() : "")
                        .setAccountId(account.getId())
                        .setSellerId(seller.getId())
                        .build())
                .build();
        responseObserver.onNext(tokenRpcResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getCustomerProfileById(AccountIdRequest request, StreamObserver<GetCustomerProfileResponse> responseObserver) {
        var account = unwrapAccountOrRespondCustomer(accountRepository.findById(request.getId()), responseObserver);
        if (account == null) return;

        if (!checkCustomerExists(account, responseObserver)) return;

        var customer = account.getCustomer();
        var roles = account.getAccountRoles().stream()
                .map(item -> item.getRole().getCode())
                .toList();

        wrapCustomerProfileResponse(responseObserver, customer, account, roles);
    }

    @Override
    public void getSellerProfileById(AccountIdRequest request, StreamObserver<GetSellerProfileResponse> responseObserver) {
        var account = unwrapAccountOrRespondSeller(accountRepository.findById(request.getId()), responseObserver);
        if (account == null) return;

        if (!checkSellerExists(account, responseObserver)) return;

        var roles = account.getAccountRoles().stream()
                .map(item -> item.getRole().getCode())
                .toList();

        var seller = account.getSeller();
        wrapSellerProfileResponse(responseObserver, seller, account, roles);
    }

    @Override
    public void getSellerProfileBySellerId(AccountIdRequest request, StreamObserver<GetSellerProfileResponse> responseObserver) {
        var sellerOptional = sellerRepository.findById(request.getId());
        if (sellerOptional.isEmpty()) {
            responseObserver.onNext(GetSellerProfileResponse.newBuilder()
                    .setIsValid(false)
                    .addErrorMessages("Seller does not exist")
                    .build());
            return;
        }
        var seller = sellerOptional.get();
        var account = seller.getAccount();
        var roles = account.getAccountRoles().stream()
                .map(item -> item.getRole().getCode())
                .toList();
        wrapSellerProfileResponse(responseObserver, seller, account, roles);
    }

    @Override
    public void getCustomerProfileByCustomerId(AccountIdRequest request, StreamObserver<GetCustomerProfileResponse> responseObserver) {
        var customerOptional = customerRepository.findById(request.getId());
        if (customerOptional.isEmpty()) {
            responseObserver.onNext(GetCustomerProfileResponse.newBuilder()
                    .setIsValid(false)
                    .addErrorMessages("Customer does not exist")
                    .build());
            return;
        }
        var customer = customerOptional.get();
        var account = customer.getAccount();
        var roles = account.getAccountRoles().stream()
                .map(item -> item.getRole().getCode())
                .toList();
        wrapCustomerProfileResponse(responseObserver, customer, account, roles);
    }

    @Override
    public void getUserAccountByUserName(UsernameRequest request, StreamObserver<GetAccountResponse> responseObserver) {
        var accountOptional = accountRepository.findByUsername(request.getUsername());
        getUserAccount(responseObserver, accountOptional);
    }

    @Override
    public void getUserAccountByAccountId(AccountIdRequest request, StreamObserver<GetAccountResponse> responseObserver) {
        var accountOptional = accountRepository.findById(request.getId());
        getUserAccount(responseObserver, accountOptional);
    }

    private void getUserAccount(StreamObserver<GetAccountResponse> responseObserver, Optional<AccountEntity> accountOptional) {
        if (accountOptional.isEmpty()) {
            responseObserver.onNext(GetAccountResponse.newBuilder()
                    .setIsValid(false)
                    .addErrorMessages("Account does not exist")
                    .build());
            responseObserver.onCompleted();
            return;
        }
        var account = accountOptional.get();
        var roles = AuthUtils.convertAccountToRole(account);
        responseObserver.onNext(GetAccountResponse.newBuilder()
                .setIsValid(true)
                .addErrorMessages("")
                .setUserInfo(AccountInfo.newBuilder()
                        .setAccountId(account.getId())
                        .addAllRoles(Objects.requireNonNullElse(roles, Collections.emptyList()))
                        .setUsername(account.getUsername())
                        .setIsActive(account.isEnabled())
                        .setIsVerified(!account.isLocked())
                        .setChangedUsername(account.isChangedUsername())
                        .build()).build());
        responseObserver.onCompleted();
    }

    private void wrapSellerProfileResponse(StreamObserver<GetSellerProfileResponse> responseObserver, SellerEntity seller, AccountEntity account, List<String> roles) {
        GetSellerProfileResponse tokenRpcResponse = GetSellerProfileResponse.newBuilder()
                .setIsValid(true)
                .setUserInfo(SellerDetailInfo.newBuilder()
                        .addAllRoles(roles)
                        .setUsername(account.getUsername())
                        .setEmail(account.getEmail())
                        .setIsActive(account.isEnabled())
                        .setIsVerified(!account.isLocked())
                        .setSellerName(seller.getShopName())
                        .setAvatarUrl(seller.getAvatarUrl() != null ? seller.getAvatarUrl() : "")
                        .setAccountId(account.getId())
                        .setSellerId(seller.getId())
                        .build())
                .build();
        responseObserver.onNext(tokenRpcResponse);
        responseObserver.onCompleted();
    }

    private void wrapCustomerProfileResponse(StreamObserver<GetCustomerProfileResponse> responseObserver, CustomerEntity customer, AccountEntity account, List<String> roles) {
        GetCustomerProfileResponse tokenRpcResponse = GetCustomerProfileResponse.newBuilder()
                .setIsValid(true)
                .setUserInfo(CustomerDetailInfo.newBuilder()
                        .addAllRoles(roles)
                        .setUsername(account.getUsername())
                        .setEmail(account.getEmail())
                        .setIsActive(account.isEnabled())
                        .setIsVerified(!account.isLocked())
                        .setFirstName(customer.getFirstName())
                        .setLastName(customer.getLastName())
                        .setPhone(customer.getPhone())
                        .setAddress(customer.getAddress())
                        .setAccountId(account.getId())
                        .setCustomerId(customer.getId())
                        .setAvatarUrl(customer.getAvatarUrl() != null ? customer.getAvatarUrl() : "")
                        .build())
                .build();
        responseObserver.onNext(tokenRpcResponse);
        responseObserver.onCompleted();
    }

    private AccountEntity unwrapAccountOrRespondCustomer(Optional<AccountEntity> accountOpt, StreamObserver<GetCustomerProfileResponse> observer) {
        if (accountOpt.isEmpty()) {
            observer.onNext(GetCustomerProfileResponse.newBuilder()
                    .setIsValid(false)
                    .addErrorMessages("Account does not exist")
                    .build());
            observer.onCompleted();
            return null;
        }
        return accountOpt.get();
    }

    private AccountEntity unwrapAccountOrRespondSeller(Optional<AccountEntity> accountOpt, StreamObserver<GetSellerProfileResponse> observer) {
        if (accountOpt.isEmpty()) {
            observer.onNext(GetSellerProfileResponse.newBuilder()
                    .setIsValid(false)
                    .addErrorMessages("Account does not exist")
                    .build());
            observer.onCompleted();
            return null;
        }
        return accountOpt.get();
    }

    private AccountEntity unwrapAccountOrRespondVerify(Optional<AccountEntity> accountOpt, StreamObserver<VerifyTokenResponse> observer) {
        if (accountOpt.isEmpty()) {
            observer.onNext(VerifyTokenResponse.newBuilder()
                    .setIsValid(false)
                    .addErrorMessages("Account does not exist")
                    .build());
            observer.onCompleted();
            return null;
        }
        return accountOpt.get();
    }

    private boolean checkCustomerExists(AccountEntity account, StreamObserver<GetCustomerProfileResponse> observer) {
        if (account.getCustomer() == null) {
            observer.onNext(GetCustomerProfileResponse.newBuilder()
                    .setIsValid(false)
                    .addErrorMessages("Customer does not exist")
                    .build());
            observer.onCompleted();
            return false;
        }
        return true;
    }

    private boolean checkSellerExists(AccountEntity account, StreamObserver<GetSellerProfileResponse> observer) {
        if (account.getSeller() == null) {
            observer.onNext(GetSellerProfileResponse.newBuilder()
                    .setIsValid(false)
                    .addErrorMessages("Seller does not exist")
                    .build());
            observer.onCompleted();
            return false;
        }
        return true;
    }

    private Optional<UserClaims> getUserClaimsFromJwt(String token, TokenType tokenType) {
        JwtTokenType type = switch (tokenType) {
            case ACCESS_TOKEN -> JwtTokenType.ACCESS_TOKEN;
            case REFRESH_TOKEN -> JwtTokenType.REFRESH_TOKEN;
            case TWO_FA_TOKEN -> JwtTokenType.TWO_FA_TOKEN;
            default -> null;
        };
        if (type == null) return Optional.empty();
        return jwtService.getUserClaimsFromJwt(token, type);
    }
}
