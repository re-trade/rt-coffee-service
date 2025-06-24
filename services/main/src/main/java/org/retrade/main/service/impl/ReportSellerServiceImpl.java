package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.CreateReportSellerRequest;
import org.retrade.main.model.dto.response.ReportSellerResponse;
import org.retrade.main.model.entity.*;
import org.retrade.main.repository.*;
import org.retrade.main.service.ReportSellerService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportSellerServiceImpl implements ReportSellerService {
    private final ReportSellerRepository reportSellerRepository;
    private final ProductRepository productRepository;
    private final OrderComboRepository orderComboRepository;
    private final SellerRepository sellerRepository;
    private final AuthUtils authUtils;

    @Override
    public ReportSellerResponse createReport(CreateReportSellerRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        var customer = account.getCustomer();

        OrderComboEntity orderComboEntity = orderComboRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ValidationException("Order not found with id: " + request.getOrderId()));

        ProductEntity productEntity = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ValidationException("Product not found with id: " + request.getProductId()));

        boolean containsProduct = orderComboEntity.getOrderItems().stream()
                .anyMatch(orderItem -> orderItem.getProduct().getId().equals(request.getProductId()));

        if (!containsProduct) {
            throw new ValidationException("Order does not contain product id: " + request.getProductId());
        }

        ReportSellerEntity reportSellerEntity = ReportSellerEntity.builder()
                .typeReport(request.getTypeReport())
                .content(request.getContent())
                .orderCombo(orderComboEntity)
                .seller(productEntity.getSeller())
                .customer(customer)
                .image(request.getImage())
                .product(productEntity)
                .build();

        try {
            reportSellerRepository.save(reportSellerEntity);

            return ReportSellerResponse.builder()
                    .sellerId(reportSellerEntity.getSeller().getId())
                    .typeReport(reportSellerEntity.getTypeReport())
                    .content(reportSellerEntity.getContent())
                    .image(reportSellerEntity.getImage())
                    .createdAt(reportSellerEntity.getCreatedDate().toLocalDateTime())
                    .productId(reportSellerEntity.getProduct().getId())
                    .build();
        } catch (Exception e) {
            throw new ActionFailedException("Failed to create report: " + e.getMessage());
        }
    }

    @Override
    public List<ReportSellerResponse> getAllReportSeller(QueryWrapper queryWrapper) {
        List<ReportSellerEntity> reportSellerEntities = reportSellerRepository.findAll();

        return reportSellerEntities.stream()
                .map(entity -> ReportSellerResponse.builder()
                        .sellerId(entity.getSeller().getId())
                        .typeReport(entity.getTypeReport())
                        .content(entity.getContent())
                        .image(entity.getImage())
                        .createdAt(entity.getCreatedDate().toLocalDateTime())
                        .productId(entity.getProduct().getId())
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportSellerResponse> getAllReportBySellerId(String sellerId, QueryWrapper   queryWrapper) {
        List<ReportSellerEntity> reportSellerEntities = reportSellerRepository.findBySellerId(sellerId);
        return reportSellerEntities.stream()
                .map(entity -> ReportSellerResponse.builder()
                        .sellerId(entity.getSeller().getId())
                        .typeReport(entity.getTypeReport())
                        .content(entity.getContent())
                        .image(entity.getImage())
                        .createdAt(entity.getCreatedDate().toLocalDateTime())
                        .productId(entity.getProduct().getId())
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    public ReportSellerResponse getReportDetail(String id) {
        ReportSellerEntity reportSellerEntity = reportSellerRepository.findById(id).orElseThrow(
                () -> new ValidationException("Report not found with id: " + id)
        );
        return ReportSellerResponse.builder()
                .sellerId(reportSellerEntity.getSeller().getId())
                .typeReport(reportSellerEntity.getTypeReport())
                .content(reportSellerEntity.getContent())
                .image(reportSellerEntity.getImage())
                .createdAt(reportSellerEntity.getCreatedDate().toLocalDateTime())
                .productId(reportSellerEntity.getProduct().getId())
                .adminId(reportSellerEntity.getAccount() != null ? reportSellerEntity.getAccount().getId() : null)
                .customerId(reportSellerEntity.getCustomer().getId())
                .resolutionDetail(reportSellerEntity.getResolutionDetail())
                .resolutionStatus(reportSellerEntity.getResolutionStatus())
                .resolutionDate(reportSellerEntity.getResolutionDate() != null
                        ? reportSellerEntity.getResolutionDate().toLocalDateTime()
                        : null)
                .orderId(reportSellerEntity.getOrderCombo().getId())
                .reportSellerId(reportSellerEntity.getId())
                .build();
    }

    @Override
    public ReportSellerResponse acceptReportSeller(String id, boolean accepted) {
        ReportSellerEntity reportSellerEntity = reportSellerRepository.findById(id).orElseThrow(
                () -> new ValidationException("Report not found with id: " + id)
        );
        if (accepted) {
            reportSellerEntity.setResolutionStatus("ACCEPT");
        }else {
            reportSellerEntity.setResolutionStatus("REJECT");
        }
        try {
            reportSellerRepository.save(reportSellerEntity);
            return ReportSellerResponse.builder()
                    .sellerId(reportSellerEntity.getSeller().getId())
                    .typeReport(reportSellerEntity.getTypeReport())
                    .content(reportSellerEntity.getContent())
                    .image(reportSellerEntity.getImage())
                    .createdAt(reportSellerEntity.getCreatedDate().toLocalDateTime())
                    .productId(reportSellerEntity.getProduct().getId())
                    .adminId(reportSellerEntity.getAccount() != null ? reportSellerEntity.getAccount().getId() : null)
                    .customerId(reportSellerEntity.getCustomer().getId())
                    .resolutionDetail(reportSellerEntity.getResolutionDetail())
                    .resolutionStatus(reportSellerEntity.getResolutionStatus())
                    .resolutionDate(reportSellerEntity.getResolutionDate() != null
                            ? reportSellerEntity.getResolutionDate().toLocalDateTime()
                            : null)
                    .orderId(reportSellerEntity.getOrderCombo().getId())
                    .reportSellerId(reportSellerEntity.getId())
                    .build();
        }catch (Exception e) {
            throw new ValidationException("Failed to create report: " + e.getMessage());
        }

    }

    @Override
    public ReportSellerResponse processReportSeller(String id, String resolutionDetail) {
        return null;
    }

}
