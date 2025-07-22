package org.retrade.main.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.OrderStatusCodes;
import org.retrade.main.model.dto.request.CreateReportSellerRequest;
import org.retrade.main.model.dto.response.ReportSellerResponse;
import org.retrade.main.model.entity.OrderComboEntity;
import org.retrade.main.model.entity.OrderStatusEntity;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.ReportSellerEntity;
import org.retrade.main.repository.jpa.*;
import org.retrade.main.service.ReportSellerService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportSellerServiceImpl implements ReportSellerService {
    private final ReportSellerRepository reportSellerRepository;
    private final ProductRepository productRepository;
    private final OrderComboRepository orderComboRepository;
    private final SellerRepository sellerRepository;
    private final OrderItemRepository orderItemRepository;
    private final AuthUtils authUtils;

    @Override
    public ReportSellerResponse createReport(CreateReportSellerRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        var customer = account.getCustomer();

        validateReportSummitRequest(request);
        OrderComboEntity orderComboEntity = orderComboRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ValidationException("Order not found with id: " + request.getOrderId()));

        ProductEntity productEntity = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ValidationException("Product not found with id: " + request.getProductId()));


        validateReportOnOrderComboStatus(orderComboEntity.getOrderStatus());

        boolean containsProduct = orderComboEntity.getOrderItems().stream()
                .anyMatch(orderItem -> orderItem.getProduct().getId().equals(request.getProductId()));

        if (!containsProduct) {
            throw new ValidationException("Order does not contain product id: " + request.getProductId());
        }

        ReportSellerEntity reportSellerEntity = ReportSellerEntity.builder()
                .typeReport(request.getTypeReport())
                .content(request.getContent())
                .orderCombo(orderComboEntity)
                .resolutionStatus("PENDING")
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
    public PaginationWrapper<List<ReportSellerResponse>> getAllReportSeller(QueryWrapper queryWrapper) {
        return reportSellerRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::mapToReportSellerResponse).stream().toList();
            return new PaginationWrapper.Builder<List<ReportSellerResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    public PaginationWrapper<List<ReportSellerResponse>> getAllReportBySellerId(String sellerId, QueryWrapper queryWrapper) {
        return reportSellerRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("seller").get("id"), sellerId));
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::mapToReportSellerResponse).stream().toList();
            return new PaginationWrapper.Builder<List<ReportSellerResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    public ReportSellerResponse getReportDetail(String id) {
        ReportSellerEntity reportSellerEntity = reportSellerRepository.findById(id).orElseThrow(
                () -> new ValidationException("Report not found with id: " + id)
        );
        return mapToReportSellerResponse(reportSellerEntity);
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
            var result = reportSellerRepository.save(reportSellerEntity);
            return mapToReportSellerResponse(result);
        }catch (Exception e) {
            throw new ValidationException("Failed to create report: " + e.getMessage());
        }

    }

    @Override
    public ReportSellerResponse acceptReport(String reportId) {
        ReportSellerEntity reportSellerEntity = reportSellerRepository.findById(reportId).orElseThrow(
                () -> new ValidationException("Report not found with id: " + reportId)
        );
        reportSellerEntity.setResolutionStatus("ACCEPTED");
        reportSellerRepository.save(reportSellerEntity);
        return mapToReportSellerResponse(reportSellerEntity);
    }

    @Override
    public ReportSellerResponse rejectReport(String reportId) {
        ReportSellerEntity reportSellerEntity = reportSellerRepository.findById(reportId).orElseThrow(
                () -> new ValidationException("Report not found with id: " + reportId)
        );
        reportSellerEntity.setResolutionStatus("REJECTED");
        reportSellerRepository.save(reportSellerEntity);
        return mapToReportSellerResponse(reportSellerEntity);    }




    @Override
    public ReportSellerResponse processReportSeller(String id, String resolutionDetail) {
        return null;
    }

    private void validateReportSummitRequest(CreateReportSellerRequest request) {
        var comboExisted = orderComboRepository.existsById(request.getOrderId());
        var productExisted = productRepository.existsById(request.getProductId());
        var sellerExisted = sellerRepository.existsById(request.getProductId());
        var productComboExisted = orderItemRepository.existsById(request.getProductId());
        if (!comboExisted) {
            throw new ValidationException("Order combo not found with id: " + request.getOrderId());
        }
        if (!productExisted) {
            throw new ValidationException("Product not found with id: " + request.getProductId());
        }
        if (!sellerExisted) {
            throw new ValidationException("Seller not found with id: " + request.getSellerId());
        }
        if (!productComboExisted) {
            throw new ValidationException("Product combo not found with id: " + request.getProductId());
        }
        if (request.getTypeReport() == null) {
            throw new ValidationException("Type report is not valid");
        }
        if (request.getContent() == null) {
            throw new ValidationException("Content is not valid");
        }
    }

    private void validateReportOnOrderComboStatus(OrderStatusEntity orderStatus) {
        if (orderStatus == null) {
            throw new ValidationException("Order status is not valid");
        }
        var allowedStatus = Set.of(
                OrderStatusCodes.DELIVERED,
                OrderStatusCodes.COMPLETED,
                OrderStatusCodes.RETURN_REQUESTED,
                OrderStatusCodes.RETURN_APPROVED,
                OrderStatusCodes.RETURN_REJECTED,
                OrderStatusCodes.RETURNED
        );
        if(allowedStatus.contains(orderStatus.getCode())) {
            throw new ValidationException("This order is not in right status");
        }
    }

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<ReportSellerEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = reportSellerRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private ReportSellerResponse mapToReportSellerResponse(ReportSellerEntity reportSellerEntity) {
        var seller = reportSellerEntity.getSeller();
        var order = reportSellerEntity.getOrderCombo();
        var admin = reportSellerEntity.getAccount();
        var customer = reportSellerEntity.getCustomer();
        return ReportSellerResponse.builder()
                .sellerId(seller.getId())
                .typeReport(reportSellerEntity.getTypeReport())
                .content(reportSellerEntity.getContent())
                .image(reportSellerEntity.getImage())
                .createdAt(reportSellerEntity.getCreatedDate().toLocalDateTime())
                .productId(reportSellerEntity.getProduct().getId())
                .adminId(admin != null ? reportSellerEntity.getAccount().getId() : null)
                .customerId(customer != null ? reportSellerEntity.getCustomer().getId() : null)
                .resolutionDetail(reportSellerEntity.getResolutionDetail())
                .resolutionStatus(reportSellerEntity.getResolutionStatus())
                .resolutionDate(reportSellerEntity.getResolutionDate() != null
                        ? reportSellerEntity.getResolutionDate().toLocalDateTime()
                        : null)
                .orderId(order != null ? order.getId() : null)
                .reportSellerId(reportSellerEntity.getId())
                .build();
    }

}