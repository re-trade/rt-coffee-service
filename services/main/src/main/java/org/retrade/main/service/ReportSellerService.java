package org.retrade.main.service;

import co.elastic.clients.elasticsearch.snapshot.CreateRepositoryRequest;
import jakarta.validation.Valid;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.main.model.dto.request.CreateReportSellerRequest;
import org.retrade.main.model.dto.response.ReportSellerResponse;

import java.util.List;

public interface ReportSellerService {
    ReportSellerResponse createReport( CreateReportSellerRequest request);

    List<ReportSellerResponse> getAllReportSeller(QueryWrapper queryWrapper);

    List<ReportSellerResponse> getAllReportBySellerId(String sellerId, QueryWrapper queryWrapper);

    ReportSellerResponse getReportDetail(String id);

    ReportSellerResponse acceptReportSeller(String id, boolean accepted);
    ReportSellerResponse acceptReport(String reportId);
    ReportSellerResponse rejectReport(String reportId);



    ReportSellerResponse processReportSeller(String id, String resolutionDetail);
}
