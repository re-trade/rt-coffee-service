package org.retrade.voucher.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.voucher.model.dto.request.CreateVoucherRequest;
import org.retrade.voucher.model.dto.request.UpdateVoucherRequest;
import org.retrade.voucher.model.dto.response.VoucherResponse;
import org.retrade.voucher.model.entity.VoucherEntity;
import org.retrade.voucher.model.entity.VoucherRestrictionEntity;
import org.retrade.voucher.repository.VoucherRepository;
import org.retrade.voucher.repository.VoucherRestrictionRepository;
import org.retrade.voucher.service.VoucherService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {
    private final VoucherRepository voucherRepository;
    private final VoucherRestrictionRepository voucherRestrictionRepository;

    @Override
    @Transactional
    public VoucherResponse createVoucher(CreateVoucherRequest request) {
        if (voucherRepository.findByCode(request.getCode()).isPresent()) {
            throw new ValidationException("Voucher with code " + request.getCode() + " already exists");
        }
        VoucherEntity voucherEntity = getVoucherEntity(request);
        VoucherEntity savedVoucher = voucherRepository.save(voucherEntity);
        if (request.getProductRestrictions() != null && !request.getProductRestrictions().isEmpty()) {
            saveVoucher(savedVoucher, request.getProductRestrictions());
        }
        return mapToVoucherResponse(savedVoucher, getProductRestrictions(savedVoucher));
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucher(String id, UpdateVoucherRequest request) {
        VoucherEntity voucherEntity = getVoucherEntityById(id);

        if (request.getCode() != null) {
            voucherRepository.findByCode(request.getCode())
                    .ifPresent(existingVoucher -> {
                        if (!existingVoucher.getId().equals(id)) {
                            throw new ValidationException("Voucher with code " + request.getCode() + " already exists");
                        }
                    });
            voucherEntity.setCode(request.getCode());
        }

        if (request.getType() != null) {
            voucherEntity.setType(request.getType().name());
        }

        if (request.getDiscount() != null) {
            voucherEntity.setDiscount(request.getDiscount());
        }

        if (request.getStartDate() != null) {
            voucherEntity.setStartDate(request.getStartDate());
        }

        if (request.getExpiryDate() != null) {
            voucherEntity.setExpiryDate(request.getExpiryDate());
        }

        if (request.getActive() != null) {
            voucherEntity.setActived(request.getActive());
        }

        if (request.getMaxUses() != null) {
            voucherEntity.setMaxUses(request.getMaxUses());
        }

        if (request.getMaxUsesPerUser() != null) {
            voucherEntity.setMaxUsesPerUser(request.getMaxUsesPerUser());
        }

        if (request.getMinSpend() != null) {
            voucherEntity.setMinSpend(request.getMinSpend());
        }

        VoucherEntity updatedVoucher = voucherRepository.save(voucherEntity);
        if (request.getProductRestrictions() != null) {
            voucherRestrictionRepository.deleteByVoucher(updatedVoucher);

            if (!request.getProductRestrictions().isEmpty()) {
                saveVoucher(updatedVoucher, request.getProductRestrictions());
            }
        }

        return mapToVoucherResponse(updatedVoucher, getProductRestrictions(updatedVoucher));
    }

    @Override
    @Transactional
    public void deleteVoucher(String id) {
        VoucherEntity voucherEntity = getVoucherEntityById(id);
        voucherRestrictionRepository.deleteByVoucher(voucherEntity);
        voucherRepository.delete(voucherEntity);
    }

    @Override
    public VoucherResponse getVoucherById(String id) {
        VoucherEntity voucherEntity = getVoucherEntityById(id);
        return mapToVoucherResponse(voucherEntity, getProductRestrictions(voucherEntity));
    }

    @Override
    public VoucherResponse getVoucherByCode(String code) {
        VoucherEntity voucherEntity = getVoucherEntityByCode(code);
        return mapToVoucherResponse(voucherEntity, getProductRestrictions(voucherEntity));
    }

    @Override
    public List<VoucherResponse> getActiveVouchers() {
        List<VoucherEntity> activeVouchers = voucherRepository.findByActived(true);
        return activeVouchers.stream()
                .map(voucher -> mapToVoucherResponse(voucher, getProductRestrictions(voucher)))
                .collect(Collectors.toList());
    }

    @Override
    public PaginationWrapper<List<VoucherResponse>> getVouchers(QueryWrapper queryWrapper) {
        Page<VoucherEntity> voucherPage = voucherRepository.queryAny(queryWrapper, queryWrapper.pagination());
        
        List<VoucherResponse> voucherResponses = voucherPage.getContent().stream()
                .map(voucher -> mapToVoucherResponse(voucher, getProductRestrictions(voucher)))
                .collect(Collectors.toList());
        
        return new PaginationWrapper.Builder<List<VoucherResponse>>()
                .setData(voucherResponses)
                .setPaginationInfo(voucherPage)
                .build();
    }

    @Override
    public VoucherEntity getVoucherEntityById(String id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Voucher not found with id: " + id));
    }

    @Override
    public VoucherEntity getVoucherEntityByCode(String code) {
        return voucherRepository.findByCode(code)
                .orElseThrow(() -> new ValidationException("Voucher not found with code: " + code));
    }

    private List<String> getProductRestrictions(VoucherEntity voucher) {
        List<VoucherRestrictionEntity> restrictions = voucherRestrictionRepository.findByVoucher(voucher);
        return restrictions.stream()
                .map(VoucherRestrictionEntity::getProductId)
                .collect(Collectors.toList());
    }

    private VoucherResponse mapToVoucherResponse(VoucherEntity entity, List<String> productRestrictions) {
        return VoucherResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .type(org.retrade.voucher.model.constant.VoucherTypeEnum.valueOf(entity.getType()))
                .discount(entity.getDiscount())
                .startDate(entity.getStartDate())
                .expiryDate(entity.getExpiryDate())
                .active(entity.getActived())
                .maxUses(entity.getMaxUses())
                .maxUsesPerUser(entity.getMaxUsesPerUser())
                .currentUses(entity.getCurrentUses())
                .minSpend(entity.getMinSpend())
                .productRestrictions(productRestrictions)
                .build();
    }

    private void saveVoucher(VoucherEntity savedVoucher, List<String> productRestrictions) {
        List<VoucherRestrictionEntity> restrictions = new ArrayList<>();
        for (String productId : productRestrictions) {
            VoucherRestrictionEntity restriction = new VoucherRestrictionEntity();
            restriction.setVoucher(savedVoucher);
            restriction.setProductId(productId);
            restrictions.add(restriction);
        }
        voucherRestrictionRepository.saveAll(restrictions);
    }

    private static VoucherEntity getVoucherEntity(CreateVoucherRequest request) {
        VoucherEntity voucherEntity = new VoucherEntity();
        voucherEntity.setCode(request.getCode());
        voucherEntity.setType(request.getType().name());
        voucherEntity.setDiscount(request.getDiscount());
        voucherEntity.setStartDate(request.getStartDate());
        voucherEntity.setExpiryDate(request.getExpiryDate());
        voucherEntity.setActived(request.getActive());
        voucherEntity.setMaxUses(request.getMaxUses());
        voucherEntity.setMaxUsesPerUser(request.getMaxUsesPerUser());
        voucherEntity.setCurrentUses(0);
        voucherEntity.setMinSpend(request.getMinSpend());
        return voucherEntity;
    }
}
