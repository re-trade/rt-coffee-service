package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.constant.SenderRoleEnum;
import org.retrade.main.model.dto.request.CreateEvidenceRequest;
import org.retrade.main.model.dto.request.CreateReportSellerRequest;
import org.retrade.main.model.dto.request.ReportSellerProcessRequest;
import org.retrade.main.model.dto.response.ReportSellerEvidenceResponse;
import org.retrade.main.model.dto.response.ReportSellerResponse;

import java.util.List;

public interface ReportSellerService {
    ReportSellerResponse createReport( CreateReportSellerRequest request);

    PaginationWrapper<List<ReportSellerResponse>> getAllReportSeller(QueryWrapper queryWrapper);

    PaginationWrapper<List<ReportSellerResponse>> getAllReportBySellerId(String sellerId, QueryWrapper queryWrapper);

    PaginationWrapper<List<ReportSellerResponse>> getAllReportBySellerAuth(QueryWrapper queryWrapper);

    PaginationWrapper<List<ReportSellerResponse>> getAllReportByCustomerAuth(QueryWrapper queryWrapper);

    ReportSellerResponse getReportDetail(String id);

    ReportSellerResponse acceptReport(String reportId);
    ReportSellerResponse rejectReport(String reportId);


    ReportSellerResponse processReportSeller(String id, ReportSellerProcessRequest request);

    PaginationWrapper<List<ReportSellerEvidenceResponse>> getReportSellerEvidenceByReportId(String id, SenderRoleEnum type, QueryWrapper queryWrapper);

    ReportSellerEvidenceResponse addSellerEvidence(String reportId, CreateEvidenceRequest request);

    ReportSellerEvidenceResponse addCustomerEvidence(String reportId, CreateEvidenceRequest request);

    ReportSellerEvidenceResponse addSystemEvidence(String reportId, CreateEvidenceRequest request);

}
