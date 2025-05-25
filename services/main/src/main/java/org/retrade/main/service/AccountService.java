package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.request.UpdatePasswordRequest;
import org.retrade.main.model.dto.response.AccountResponse;

import java.util.List;

public interface AccountService {
    AccountResponse getMe();

    AccountResponse getAccountById(String id);

    void updatePassword(String id, UpdatePasswordRequest request);

    void deleteAccount(String id);

    void resetPassword(String id);

    PaginationWrapper<List<AccountResponse>> getAllAccounts(QueryWrapper queryWrapper);
}
