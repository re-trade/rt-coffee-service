package org.retrade.voucher.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.proto.voucher.*;
import org.retrade.voucher.model.constant.VoucherStatusEnum;
import org.retrade.voucher.model.constant.VoucherTypeEnum;
import org.retrade.voucher.model.dto.request.ApplyVoucherRequest;
import org.retrade.voucher.model.dto.request.ClaimVoucherRequest;
import org.retrade.voucher.model.dto.request.CreateVoucherRequest;
import org.retrade.voucher.model.dto.request.ValidateVoucherRequest;
import org.retrade.voucher.model.dto.response.VoucherClaimResponse;
import org.retrade.voucher.model.dto.response.VoucherResponse;
import org.retrade.voucher.model.dto.response.VoucherValidationResponse;
import org.retrade.voucher.service.VoucherClaimService;
import org.retrade.voucher.service.VoucherService;
import org.retrade.voucher.service.VoucherValidationService;
import org.springframework.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class VoucherGrpcServiceImpl extends GrpcVoucherServiceGrpc.GrpcVoucherServiceImplBase {
    private final VoucherService voucherService;
    private final VoucherClaimService voucherClaimService;
    private final VoucherValidationService voucherValidationService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public void createVoucher(org.retrade.proto.voucher.CreateVoucherRequest request,
                             StreamObserver<CreateVoucherResponse> responseObserver) {
        try {
            CreateVoucherRequest createRequest = convertToCreateVoucherRequest(request);
            VoucherResponse voucherResponse = voucherService.createVoucher(createRequest);
            CreateVoucherResponse response = CreateVoucherResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Voucher created successfully")
                    .setVoucher(convertToVoucherDetails(voucherResponse))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error creating voucher via gRPC", e);
            CreateVoucherResponse response = CreateVoucherResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error creating voucher: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getVoucherByCode(GetVoucherByCodeRequest request,
                                 StreamObserver<GetVoucherByCodeResponse> responseObserver) {
        try {
            VoucherResponse voucherResponse = voucherService.getVoucherByCode(request.getCode());
            GetVoucherByCodeResponse response = GetVoucherByCodeResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Voucher found")
                    .setVoucher(convertToVoucherDetails(voucherResponse))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting voucher by code via gRPC", e);
            GetVoucherByCodeResponse response = GetVoucherByCodeResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error getting voucher: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void claimVoucher(org.retrade.proto.voucher.ClaimVoucherRequest request,
                            StreamObserver<ClaimVoucherResponse> responseObserver) {
        try {
            ClaimVoucherRequest claimRequest = ClaimVoucherRequest.builder()
                    .code(request.getCode())
                    .accountId(request.getAccountId())
                    .build();
            VoucherClaimResponse claimResponse = voucherClaimService.claimVoucher(claimRequest);
            ClaimVoucherResponse response = ClaimVoucherResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Voucher claimed successfully")
                    .setVoucherClaim(convertToVoucherClaimProto(claimResponse))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error claiming voucher via gRPC", e);
            ClaimVoucherResponse response = ClaimVoucherResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error claiming voucher: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void validateVoucher(org.retrade.proto.voucher.ValidateVoucherRequest request,
                               StreamObserver<ValidateVoucherResponse> responseObserver) {
        try {
            ValidateVoucherRequest validateRequest = ValidateVoucherRequest.builder()
                    .code(request.getCode())
                    .accountId(request.getAccountId())
                    .orderTotal(BigDecimal.valueOf(request.getOrderTotal()))
                    .productIds(request.getProductIdsList())
                    .build();
            VoucherValidationResponse validationResponse = voucherValidationService.validateVoucher(validateRequest);
            ValidateVoucherResponse response = ValidateVoucherResponse.newBuilder()
                    .setValid(validationResponse.isValid())
                    .setMessage(validationResponse.getMessage())
                    .setVoucherId(validationResponse.getVoucherId() != null ? validationResponse.getVoucherId() : "")
                    .setCode(validationResponse.getCode() != null ? validationResponse.getCode() : "")
                    .setDiscountAmount(validationResponse.getDiscountAmount() != null ?
                            validationResponse.getDiscountAmount().doubleValue() : 0.0)
                    .setType(validationResponse.getType() != null ?
                            convertToVoucherTypeProto(VoucherTypeEnum.valueOf(validationResponse.getType())) :
                            VoucherTypeProto.VOUCHER_TYPE_UNSPECIFIED)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error validating voucher via gRPC", e);
            ValidateVoucherResponse response = ValidateVoucherResponse.newBuilder()
                    .setValid(false)
                    .setMessage("Error validating voucher: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void applyVoucher(org.retrade.proto.voucher.ApplyVoucherRequest request,
                            StreamObserver<ApplyVoucherResponse> responseObserver) {
        try {
            ApplyVoucherRequest applyRequest = ApplyVoucherRequest.builder()
                    .code(request.getCode())
                    .accountId(request.getAccountId())
                    .orderId(request.getOrderId())
                    .orderTotal(BigDecimal.valueOf(request.getOrderTotal()))
                    .build();
            VoucherValidationResponse validationResponse = voucherValidationService.applyVoucher(applyRequest);
            ApplyVoucherResponse response = ApplyVoucherResponse.newBuilder()
                    .setSuccess(validationResponse.isValid())
                    .setMessage(validationResponse.getMessage())
                    .setDiscountAmount(validationResponse.getDiscountAmount() != null ?
                            validationResponse.getDiscountAmount().doubleValue() : 0.0)
                    .setType(validationResponse.getType() != null ?
                            convertToVoucherTypeProto(VoucherTypeEnum.valueOf(validationResponse.getType())) :
                            VoucherTypeProto.VOUCHER_TYPE_UNSPECIFIED)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error applying voucher via gRPC", e);
            ApplyVoucherResponse response = ApplyVoucherResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error applying voucher: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getUserVouchers(GetUserVouchersRequest request,
                               StreamObserver<GetUserVouchersResponse> responseObserver) {
        try {
            List<VoucherClaimResponse> userVouchers;
            if (request.getActiveOnly()) {
                userVouchers = voucherClaimService.getUserActiveVouchers(request.getAccountId());
            } else {
                userVouchers = voucherClaimService.getUserVouchers(request.getAccountId());
            }
            List<VoucherClaimProto> voucherClaimProtos = userVouchers.stream()
                    .map(this::convertToVoucherClaimProto)
                    .collect(Collectors.toList());
            GetUserVouchersResponse response = GetUserVouchersResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User vouchers retrieved successfully")
                    .addAllVouchers(voucherClaimProtos)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting user vouchers via gRPC", e);
            GetUserVouchersResponse response = GetUserVouchersResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error getting user vouchers: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private CreateVoucherRequest convertToCreateVoucherRequest(org.retrade.proto.voucher.CreateVoucherRequest proto) {
        return CreateVoucherRequest.builder()
                .code(proto.getCode())
                .type(convertToVoucherTypeEnum(proto.getType()))
                .discount(proto.getDiscount())
                .startDate(LocalDateTime.parse(proto.getStartDate(), DATE_FORMATTER))
                .expiryDate(LocalDateTime.parse(proto.getExpiryDate(), DATE_FORMATTER))
                .active(proto.getActive())
                .maxUses(proto.getMaxUses())
                .maxUsesPerUser(proto.getMaxUsesPerUser())
                .minSpend(new BigDecimal(proto.getMinSpend()))
                .productRestrictions(new ArrayList<>(proto.getProductRestrictionsList()))
                .build();
    }

    private VoucherDetails convertToVoucherDetails(VoucherResponse dto) {
        return VoucherDetails.newBuilder()
                .setId(dto.getId())
                .setCode(dto.getCode())
                .setType(convertToVoucherTypeProto(dto.getType()))
                .setDiscount(dto.getDiscount())
                .setStartDate(dto.getStartDate().format(DATE_FORMATTER))
                .setExpiryDate(dto.getExpiryDate().format(DATE_FORMATTER))
                .setActive(dto.getActive())
                .setMaxUses(dto.getMaxUses())
                .setMaxUsesPerUser(dto.getMaxUsesPerUser())
                .setMinSpend(dto.getMinSpend().toEngineeringString())
                .addAllProductRestrictions(dto.getProductRestrictions())
                .build();
    }

    private VoucherClaimProto convertToVoucherClaimProto(VoucherClaimResponse dto) {
        return VoucherClaimProto.newBuilder()
                .setId(dto.getId())
                .setVoucherId(dto.getVoucherId())
                .setCode(dto.getCode())
                .setType(convertToVoucherTypeProto(VoucherTypeEnum.valueOf(dto.getType())))
                .setDiscount(dto.getDiscount())
                .setExpiryDate(dto.getExpiryDate().format(DATE_FORMATTER))
                .setStatus(convertToVoucherStatusProto(VoucherStatusEnum.valueOf(dto.getStatus())))
                .build();
    }

    private VoucherTypeEnum convertToVoucherTypeEnum(VoucherTypeProto proto) {
        if (Objects.requireNonNull(proto) == VoucherTypeProto.PERCENTAGE) {
            return VoucherTypeEnum.PERCENTAGE;
        }
        return VoucherTypeEnum.FIXED_AMOUNT;
    }

    private VoucherTypeProto convertToVoucherTypeProto(VoucherTypeEnum type) {
        return switch (type) {
            case PERCENTAGE -> VoucherTypeProto.PERCENTAGE;
            case FIXED_AMOUNT -> VoucherTypeProto.FIXED_AMOUNT;
            default -> VoucherTypeProto.VOUCHER_TYPE_UNSPECIFIED;
        };
    }

    private VoucherStatusProto convertToVoucherStatusProto(VoucherStatusEnum status) {
        return switch (status) {
            case ACTIVE -> VoucherStatusProto.ACTIVE;
            case USED -> VoucherStatusProto.USED;
            case EXPIRED -> VoucherStatusProto.EXPIRED;
            case INACTIVE -> VoucherStatusProto.INACTIVE;
            default -> VoucherStatusProto.VOUCHER_STATUS_UNSPECIFIED;
        };
    }
}
