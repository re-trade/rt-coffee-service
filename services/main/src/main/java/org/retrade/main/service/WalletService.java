package org.retrade.main.service;

import org.retrade.main.model.dto.request.WithdrawRequest;
import org.retrade.main.model.dto.response.AccountWalletResponse;

public interface WalletService {
    AccountWalletResponse getUserAccountWallet();

    void withdrawRequest(WithdrawRequest request);

    void approveWithdrawRequest(String withdrawRequestId);
}
