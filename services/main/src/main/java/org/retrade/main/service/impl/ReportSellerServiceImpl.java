package org.retrade.main.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.OrderStatusCodes;
import org.retrade.main.model.constant.ReportSellerStatusCodes;
import org.retrade.main.model.constant.SenderRoleEnum;
import org.retrade.main.model.dto.request.CreateEvidenceRequest;
import org.retrade.main.model.dto.request.CreateReportSellerRequest;
import org.retrade.main.model.dto.request.ReportSellerProcessRequest;
import org.retrade.main.model.dto.response.ReportSellerEvidenceResponse;
import org.retrade.main.model.dto.response.ReportSellerResponse;
import org.retrade.main.model.entity.*;
import org.retrade.main.repository.jpa.*;
import org.retrade.main.service.ReportSellerService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportSellerServiceImpl implements ReportSellerService {
    private final ReportSellerRepository reportSellerRepository;
    private final ProductRepository productRepository;
    private final OrderComboRepository orderComboRepository;
    private final SellerRepository sellerRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReportSellerHistoryRepository reportSellerHistoryRepository;
    private final ReportSellerEvidenceRepository reportSellerEvidenceRepository;
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

        ReportSellerEntity reportSellerEntity = ReportSellerEntity.builder()
                .typeReport(request.getTypeReport())
                .content(request.getContent())
                .orderCombo(orderComboEntity)
                .resolutionStatus(ReportSellerStatusCodes.PENDING)
                .seller(productEntity.getSeller())
                .customer(customer)
                .resolutionDetail("")
                .product(productEntity)
                .build();

        ReportSellerEvidenceEntity reportSellerEvidenceEntity = ReportSellerEvidenceEntity.builder()
                .reportSeller(reportSellerEntity)
                .senderRole(SenderRoleEnum.CUSTOMER)
                .note(request.getContent())
                .evidenceUrls(request.getEvidenceUrls())
                .sender(account)
                .build();

        reportSellerEntity.setReportSellerEvidence(Set.of(reportSellerEvidenceEntity));

        try {
            var result = reportSellerRepository.save(reportSellerEntity);
            return mapToReportSellerResponse(result);
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
    public PaginationWrapper<List<ReportSellerResponse>> getAllReportBySellerAuth(QueryWrapper queryWrapper) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.getSeller() == null) {
            throw new ValidationException("User is not a seller, please register seller or contact with Admin");
        }
        var seller = account.getSeller();
        return reportSellerRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("seller"), seller));
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
    public PaginationWrapper<List<ReportSellerResponse>> getAllReportByCustomerAuth(QueryWrapper queryWrapper) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.getCustomer() == null) {
            throw new ValidationException("User is not a customer, please register customer or contact with Admin");
        }
        var customer = account.getCustomer();
        return reportSellerRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("customer"), customer));
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
    public ReportSellerResponse acceptReport(String reportId) {
        ReportSellerEntity reportSellerEntity = reportSellerRepository.findById(reportId).orElseThrow(
                () -> new ValidationException("Report not found with id: " + reportId)
        );
        reportSellerEntity.setResolutionStatus(ReportSellerStatusCodes.ACCEPTED);
        reportSellerRepository.save(reportSellerEntity);
        return mapToReportSellerResponse(reportSellerEntity);
    }

    @Override
    public ReportSellerResponse rejectReport(String reportId) {
        ReportSellerEntity reportSellerEntity = reportSellerRepository.findById(reportId).orElseThrow(
                () -> new ValidationException("Report not found with id: " + reportId)
        );
        reportSellerEntity.setResolutionStatus(ReportSellerStatusCodes.REJECTED);
        reportSellerRepository.save(reportSellerEntity);
        return mapToReportSellerResponse(reportSellerEntity);
    }

    @Override
    public ReportSellerResponse processReportSeller(String id, ReportSellerProcessRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (!AuthUtils.convertAccountToRole(account).contains("ROLE_ADMIN")) {
            throw new ValidationException("User is not a admin");
        }
        var reportSellerEntity = reportSellerRepository.findById(id).orElseThrow(
                () -> new ValidationException("Report not found with id: " + id)
        );
        var history  = ReportSellerHistoryEntity.builder()
                .admin(account)
                .actionType("SYSTEM_ADDED_EVIDENCE")
                .notes("System process evidence to report " + id + " by admin " + account.getId() + " with notes: " + request.getNotes());
        if (request.getAccepted() == null) {
            reportSellerEntity.setResolutionDetail(request.getResolutionDetail());
            history.actionType("SYSTEM_UPDATED_REPORT_RESOLUTION_DETAIL");
        } else {
            reportSellerEntity.setResolutionStatus(request.getAccepted() ? ReportSellerStatusCodes.ACCEPTED : ReportSellerStatusCodes.REJECTED);
            reportSellerEntity.setResolutionDetail(request.getResolutionDetail());
            history.actionType("SYSTEM_UPDATED_REPORT_RESOLUTION_STATUS");
        }
        reportSellerEntity.setResolutionDate(Timestamp.valueOf(LocalDateTime.now()));
        try {
            var result = reportSellerRepository.save(reportSellerEntity);
            history.reportSeller(result);
            return mapToReportSellerResponse(result);
        } catch (Exception e) {
            throw new ActionFailedException("Failed to process report: " + e.getMessage());
        }
    }

    @Override
    public PaginationWrapper<List<ReportSellerEvidenceResponse>> getReportSellerEvidenceByReportId(String id, SenderRoleEnum type, QueryWrapper queryWrapper) {
        var account = authUtils.getUserAccountFromAuthentication();

        return reportSellerEvidenceRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("reportSeller").get("id"), id));
            if (!AuthUtils.convertAccountToRole(account).contains("ROLE_ADMIN")) {
                var reportSellerJoin = root.join("reportSeller", JoinType.INNER);
                switch (type) {
                    case CUSTOMER:
                        if (account.getCustomer() == null) {
                            throw new ValidationException("User is not a customer, please register customer or contact with Admin");
                        }
                        predicates.add(criteriaBuilder.equal(root.get("sender").get("id"), account.getId()));
                        predicates.add(criteriaBuilder.equal(reportSellerJoin.get("customer").get("id"), account.getCustomer().getId()));
                        break;
                    case SELLER:
                        if (account.getSeller() == null) {
                            throw new ValidationException("User is not a seller, please register seller or contact with Admin");
                        }
                        predicates.add(criteriaBuilder.equal(reportSellerJoin.get("seller").get("id"), account.getSeller().getId()));
                        break;
                }
            }
            return getReportSellerEvidencePredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(item -> this.mapToReportSellerEvidenceResponse(item, type)).stream().toList();
            return new PaginationWrapper.Builder<List<ReportSellerEvidenceResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    public ReportSellerEvidenceResponse addSellerEvidence(String reportId, CreateEvidenceRequest request) {
        var report = reportSellerRepository.findById(reportId).orElseThrow(() -> new ValidationException("Not found report with id: " + reportId));
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.getSeller() == null) {
            throw new ValidationException("User is not a seller, please register seller or contact with Admin");
        }
        if (Set.of("ACCEPTED", "REJECTED").contains(report.getResolutionStatus())) {
            throw new ValidationException("Report is already accepted or rejected");
        }
        if (!Objects.equals(report.getSeller().getId(), account.getSeller().getId())) {
            throw new ValidationException("User is not a seller, please register seller or contact with Admin");
        }
        return addEvidence(report, account, request, SenderRoleEnum.SELLER);
    }

    @Override
    public ReportSellerEvidenceResponse addCustomerEvidence(String reportId, CreateEvidenceRequest request) {
        var report = reportSellerRepository.findById(reportId).orElseThrow(() -> new ValidationException("Not found report with id: " + reportId));
        var account = authUtils.getUserAccountFromAuthentication();
        if (!Objects.equals(report.getCustomer().getId(), account.getCustomer().getId())) {
            throw new ValidationException("User is not a seller, please register seller or contact with Admin");
        }
        return addEvidence(report, account, request, SenderRoleEnum.CUSTOMER);
    }

    @Override
    public ReportSellerEvidenceResponse addSystemEvidence(String reportId, CreateEvidenceRequest request) {
        var report = reportSellerRepository.findById(reportId).orElseThrow(() -> new ValidationException("Not found report with id: " + reportId));
        var account = authUtils.getUserAccountFromAuthentication();
        if (!AuthUtils.convertAccountToRole(account).contains("ROLE_ADMIN")) {
            throw new ValidationException("User is not a admin");
        }
        var history  = ReportSellerHistoryEntity.builder()
                .reportSeller(report)
                .admin(account)
                .actionType("SYSTEM_ADDED_EVIDENCE")
                .notes("System adding evidence to report " + reportId + " by admin " + account.getId() + " with notes: " + request.getNote() + " and evidence urls: " + request.getEvidenceUrls())
                .build();
        reportSellerHistoryRepository.save(history);
        return addEvidence(report, account, request, SenderRoleEnum.SYSTEM);
    }


    private ReportSellerEvidenceResponse addEvidence(ReportSellerEntity report, AccountEntity account , CreateEvidenceRequest request, SenderRoleEnum type) {
        if (Set.of("ACCEPTED", "REJECTED").contains(report.getResolutionStatus())) {
            throw new ValidationException("Report is already accepted or rejected");
        }
        var evidenceEntity = ReportSellerEvidenceEntity.builder()
                .reportSeller(report)
                .senderRole(type)
                .note(request.getNote())
                .evidenceUrls(request.getEvidenceUrls())
                .sender(account)
                .build();
        report.getReportSellerEvidence().add(evidenceEntity);
        var result = reportSellerEvidenceRepository.save(evidenceEntity);
        return mapToReportSellerEvidenceResponse(result, type);
    }

    private void validateReportSummitRequest(CreateReportSellerRequest request) {
        var comboExisted = orderComboRepository.existsById(request.getOrderId());
        var productExisted = productRepository.existsById(request.getProductId());
        var sellerExisted = sellerRepository.existsById(request.getSellerId());
        var productComboExisted = orderItemRepository.existsByProduct_IdAndOrderCombo_Id(request.getProductId(), request.getOrderId());
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
                OrderStatusCodes.RETRIEVED,
                OrderStatusCodes.COMPLETED,
                OrderStatusCodes.RETURN_REQUESTED,
                OrderStatusCodes.RETURN_APPROVED,
                OrderStatusCodes.RETURN_REJECTED,
                OrderStatusCodes.RETURNED
        );
        if(!allowedStatus.contains(orderStatus.getCode())) {
            throw new ValidationException("This order is not in right status");
        }
    }

    private Predicate getReportSellerEvidencePredicate(Map<String, QueryFieldWrapper> param, Root<ReportSellerEvidenceEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = reportSellerEvidenceRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<ReportSellerEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = reportSellerRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private ReportSellerEvidenceResponse mapToReportSellerEvidenceResponse(ReportSellerEvidenceEntity reportSellerEvidenceEntity, SenderRoleEnum typeMapping) {
        var response =  ReportSellerEvidenceResponse.builder()
                .id(reportSellerEvidenceEntity.getId())
                .createdAt(reportSellerEvidenceEntity.getCreatedDate().toLocalDateTime())
                .senderRole(reportSellerEvidenceEntity.getSenderRole())
                .senderId(reportSellerEvidenceEntity.getSender() != null ? reportSellerEvidenceEntity.getSender().getId() : null)
                .notes(reportSellerEvidenceEntity.getNote())
                .evidenceUrls(reportSellerEvidenceEntity.getEvidenceUrls());
        if (Objects.requireNonNull(typeMapping) == SenderRoleEnum.SELLER
                && reportSellerEvidenceEntity.getSenderRole() == SenderRoleEnum.CUSTOMER) {
            return response
                    .senderId("N/A")
                    .senderName("N/A")
                    .senderAvatarUrl("N/A")
                    .build();
        }
        String senderName = "N/A";
        String senderAvatarUrl = "N/A";

        var sender = reportSellerEvidenceEntity.getSender();

        if (reportSellerEvidenceEntity.getSenderRole() == SenderRoleEnum.CUSTOMER) {
            var customer = sender.getCustomer();
            if (customer != null) {
                senderName = String.format("%s %s", customer.getFirstName(), customer.getLastName());
                senderAvatarUrl = customer.getAvatarUrl();
            }
        } else if (reportSellerEvidenceEntity.getSenderRole() == SenderRoleEnum.SELLER) {
            var seller = sender.getSeller();
            if (seller != null) {
                senderName = seller.getShopName();
                senderAvatarUrl = seller.getAvatarUrl();
            }
        }
        return response
                .senderId(sender.getId())
                .senderName(senderName)
                .senderAvatarUrl(senderAvatarUrl)
                .build();
    }

    private ReportSellerResponse mapToReportSellerResponse(ReportSellerEntity reportSellerEntity) {
        var seller = reportSellerEntity.getSeller();
        var order = reportSellerEntity.getOrderCombo();
        var customer = reportSellerEntity.getCustomer();
        return ReportSellerResponse.builder()
                .id(reportSellerEntity.getId())
                .sellerId(seller.getId())
                .sellerName(seller.getShopName())
                .sellerAvatarUrl(seller.getAvatarUrl())
                .typeReport(reportSellerEntity.getTypeReport())
                .content(reportSellerEntity.getContent())
                .createdAt(reportSellerEntity.getCreatedDate().toLocalDateTime())
                .productId(reportSellerEntity.getProduct().getId())
                .customerId(customer != null ? reportSellerEntity.getCustomer().getId() : null)
                .resolutionDetail(reportSellerEntity.getResolutionDetail())
                .resolutionStatus(reportSellerEntity.getResolutionStatus())
                .resolutionDate(reportSellerEntity.getResolutionDate() != null
                        ? reportSellerEntity.getResolutionDate().toLocalDateTime()
                        : null)
                .orderId(order != null ? order.getId() : null)
                .build();
    }

}