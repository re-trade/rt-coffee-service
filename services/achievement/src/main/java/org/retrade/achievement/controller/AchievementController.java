package org.retrade.achievement.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.retrade.achievement.model.dto.request.AchievementConditionRequest;
import org.retrade.achievement.model.dto.request.AchievementRequest;
import org.retrade.achievement.model.dto.response.AchievementConditionResponse;
import org.retrade.achievement.model.dto.response.AchievementResponse;
import org.retrade.achievement.service.AchievementService;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("achievements")
public class AchievementController {
    private final AchievementService achievementService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<AchievementResponse>> createAchievement (@RequestBody  AchievementRequest request) {
        var response = achievementService.createAchievement(request);
        return ResponseEntity.ok(new ResponseObject.Builder<AchievementResponse>()
                .success(true)
                .code("ACHIEVEMENT_SUBMIT")
                .messages("Achievement created successfully")
                .content(response)
                .build());
    }

    @PostMapping("conditions")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<AchievementConditionResponse>> createAchievementCondition (@RequestBody AchievementConditionRequest request) {
        var response = achievementService.createAchievementCondition(request);
        return ResponseEntity.ok(new ResponseObject.Builder<AchievementConditionResponse>()
                .success(true)
                .code("ACHIEVEMENT_CONDITION_SUBMIT")
                .messages("Achievement condition created successfully")
                .content(response)
                .build());
    }

    @PutMapping("conditions/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<AchievementConditionResponse>> updateAchievementCondition (@PathVariable String id, @RequestBody AchievementConditionRequest request) {
        var response = achievementService.updateAchievementCondition(id, request);
        return ResponseEntity.ok(new ResponseObject.Builder<AchievementConditionResponse>()
                .success(true)
                .code("ACHIEVEMENT_CONDITION_SUBMIT")
                .messages("Achievement condition updated successfully")
                .content(response)
                .build());
    }

    @DeleteMapping("conditions/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<Void>> deleteAchievementCondition (@PathVariable String id) {
        achievementService.deleteAchievementCondition(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("ACHIEVEMENT_CONDITION_SUBMIT")
                .messages("Achievement condition created successfully")
                .build());
    }

    @GetMapping
    public ResponseEntity<ResponseObject<List<AchievementResponse>>> getAllAchievements(
            @Parameter(description = "Pagination parameters (page, size, sort)") @PageableDefault Pageable pageable,
            @Parameter(description = "Search query to filter achievement by name") @RequestParam(required = false, name = "q") String query
    ) {
        var wrapper = QueryWrapper.builder()
                .pageable(pageable)
                .search(query)
                .build();
        var achievements = achievementService.getAchievements(wrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<AchievementResponse>>()
                .success(true)
                .code("ACHIEVEMENTS_RETRIEVED")
                .messages("Achievements retrieved successfully")
                .unwrapPaginationWrapper(achievements)
                .build());
    }


    @GetMapping("{achievementId}")
    public ResponseEntity<ResponseObject<List<AchievementResponse>>> getAllAchievementConditionsByAchievementId(
            @Parameter(description = "Pagination parameters (page, size, sort)") @PageableDefault Pageable pageable,
            @PathVariable String achievementId,
            @Parameter(description = "Search query to filter achievement by name") @RequestParam(required = false, name = "q") String query
    ) {
        var wrapper = QueryWrapper.builder()
                .pageable(pageable)
                .search(query)
                .build();
        var achievements = achievementService.getAchievementConditionsByAchievementId(wrapper, achievementId);
        return ResponseEntity.ok(new ResponseObject.Builder<List<AchievementResponse>>()
                .success(true)
                .code("ACHIEVEMENT_CONDITIONS_RETRIEVED")
                .messages("Achievement conditions retrieved successfully")
                .unwrapPaginationWrapper(achievements)
                .build());
    }
}
