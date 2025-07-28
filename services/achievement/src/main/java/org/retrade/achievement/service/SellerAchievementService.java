package org.retrade.achievement.service;

import jakarta.servlet.http.HttpServletRequest;
import org.retrade.achievement.model.dto.response.SellerAchievementResponse;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;

import java.util.List;

public interface SellerAchievementService {
    PaginationWrapper<List<SellerAchievementResponse>> getSellerAchievementCompletedBySellerId (String sellerId, QueryWrapper queryWrapper);

    PaginationWrapper<List<SellerAchievementResponse>> getSellerAchievementCompleted(HttpServletRequest httpServletRequest, QueryWrapper queryWrapper);
}
