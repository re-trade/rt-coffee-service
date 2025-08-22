package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.request.WithdrawApproveRequest;
import org.retrade.main.model.dto.request.WithdrawRequest;
import org.retrade.main.model.dto.response.*;

import java.util.List;

public interface WalletService {
    AccountWalletResponse getUserAccountWallet();

    void withdrawRequest(WithdrawRequest request);

    void approveWithdrawRequest(WithdrawApproveRequest request);

    PaginationWrapper<List<BankResponse>> getBankList(QueryWrapper queryWrapper);

    PaginationWrapper<List<WithdrawRequestBaseResponse>> getWithdrawRequestList(QueryWrapper queryWrapper);

    PaginationWrapper<List<WithdrawRequestBaseResponse>> getAccountWithdrawRequest(QueryWrapper queryWrapper);

    BankResponse getBankByBin(String id);

    DecodedFile getQrCodeByWithdrawRequestId(String withdrawRequestId);

    WithdrawRequestDetailResponse getWithdrawRequestDetail(String withdrawRequestId);
}
