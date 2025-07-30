package org.retrade.achievement.service;

import org.retrade.achievement.model.dto.request.AchievementConditionRequest;
import org.retrade.achievement.model.dto.request.AchievementRequest;
import org.retrade.achievement.model.dto.response.AchievementConditionResponse;
import org.retrade.achievement.model.dto.response.AchievementResponse;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;

import java.util.List;

public interface AchievementService {
    AchievementResponse createAchievement(AchievementRequest request);

    AchievementConditionResponse createAchievementCondition(AchievementConditionRequest request);

    void deleteAchievement(String id);

    AchievementResponse getAchievement(String id);

    AchievementResponse activateAchievement(String id);

    AchievementConditionResponse updateAchievementCondition(String id, AchievementConditionRequest request);

    void deleteAchievementCondition(String id);

    PaginationWrapper<List<AchievementResponse>> getAchievements(QueryWrapper queryWrapper);

    PaginationWrapper<List<AchievementConditionResponse>> getAchievementConditionsByAchievementId(QueryWrapper queryWrapper, String id);
}
