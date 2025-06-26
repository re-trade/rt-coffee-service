package org.retrade.main.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.main.model.dto.request.UpdateEmailRequest;
import org.retrade.main.model.dto.request.UpdatePasswordRequest;
import org.retrade.main.model.dto.request.UpdateUsernameRequest;
import org.retrade.main.model.dto.response.AccountResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AccountService {
    AccountResponse getMe();

    boolean checkUsernameExisted(String username);

    AccountResponse getAccountById(String id);

    void updatePassword(UpdatePasswordRequest request);

    void deleteAccount(String id);

    void resetPassword(String id);

    PaginationWrapper<List<AccountResponse>> getAllAccounts(QueryWrapper queryWrapper);

    AccountResponse updateEmail(UpdateEmailRequest request);

    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    AccountResponse updateUsername(UpdateUsernameRequest updateRequest, HttpServletRequest request, HttpServletResponse response);

    AccountResponse disableCustomerAccount(String customerId);

    AccountResponse enableCustomerAccount(String customerId);
}
