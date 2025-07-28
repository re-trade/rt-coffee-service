package org.retrade.achievement.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.retrade.achievement.model.dto.request.AchievementConditionRequest;
import org.retrade.achievement.model.dto.request.AchievementRequest;
import org.retrade.achievement.model.dto.response.AchievementConditionResponse;
import org.retrade.achievement.model.dto.response.AchievementResponse;
import org.retrade.achievement.model.entity.AchievementConditionEntity;
import org.retrade.achievement.model.entity.AchievementEntity;
import org.retrade.achievement.repository.AchievementConditionRepository;
import org.retrade.achievement.repository.AchievementRepository;
import org.retrade.achievement.service.AchievementService;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {
    private final AchievementRepository achievementRepository;
    private final AchievementConditionRepository achievementConditionRepository;

    @Override
    public AchievementResponse createAchievement(AchievementRequest request) {
        var entity = AchievementEntity.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .isActivated(request.getIsActivated() != null ? request.getIsActivated() : false)
                .build();
        var result = achievementRepository.save(entity);
        return mapToAchievementResponse(result);
    }

    @Override
    public AchievementConditionResponse createAchievementCondition(AchievementConditionRequest request) {
        var achievementEntity = achievementRepository.findById(request.getAchievementId()).orElseThrow(() -> new ValidationException(""));
        if (Boolean.TRUE.equals(achievementEntity.getIsActivated())) {
            throw new ValidationException("Achievement is already activated");
        }
        var achievementConditionEntity = AchievementConditionEntity.builder()
                .achievement(achievementEntity)
                .type(request.getType())
                .threshold(request.getThreshold())
                .periodDays(request.getPeriodDays())
                .build();
        var result = achievementConditionRepository.save(achievementConditionEntity);
        return mapToAchievementConditionResponse(result);
    }

    @Override
    public void deleteAchievement(String id) {
        achievementRepository.deleteById(id);
    }

    @Override
    public AchievementResponse getAchievement(String id) {
        var entity = achievementRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Achievement not found"));
        return mapToAchievementResponse(entity);
    }

    @Override
    public AchievementResponse activateAchievement(String id) {
        var entity = achievementRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Achievement not found"));

        if (Boolean.TRUE.equals(entity.getIsActivated())) {
            throw new ValidationException("Achievement already activated");
        }

        entity.setIsActivated(true);
        var updated = achievementRepository.save(entity);
        return mapToAchievementResponse(updated);
    }

    @Override
    public AchievementConditionResponse updateAchievementCondition(String id, AchievementConditionRequest request) {
        var condition = achievementConditionRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Condition not found"));

        var achievement = achievementRepository.findById(request.getAchievementId())
                .orElseThrow(() -> new ValidationException("Achievement not found"));

        if (Boolean.TRUE.equals(achievement.getIsActivated())) {
            throw new ValidationException("Achievement is already activated");
        }

        condition.setAchievement(achievement);
        condition.setType(request.getType());
        condition.setThreshold(request.getThreshold());
        condition.setPeriodDays(request.getPeriodDays());

        var updated = achievementConditionRepository.save(condition);
        return mapToAchievementConditionResponse(updated);
    }

    @Override
    public void deleteAchievementCondition(String id) {
        achievementConditionRepository.deleteById(id);
    }

    @Override
    public PaginationWrapper<List<AchievementResponse>> getAchievements(QueryWrapper queryWrapper) {
        return achievementRepository.query(queryWrapper, (param) -> ((root, query1, criteriaBuilder) ->
        {
            List<Predicate> predicates = new ArrayList<>();
            return getAchievementPredicate(param, root, criteriaBuilder, predicates);
        }), (items) -> {
            var list = items.map(this::mapToAchievementResponse).stream().toList();
            return new PaginationWrapper.Builder<List<AchievementResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    public PaginationWrapper<List<AchievementConditionResponse>> getAchievementConditionsByAchievementId(QueryWrapper queryWrapper, String id) {
        return achievementConditionRepository.query(queryWrapper, (param) -> ((root, query1, criteriaBuilder) ->
        {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("achievement").get("id"), id));
            return getAchievementConditionPredicate(param, root, criteriaBuilder, predicates);
        }), (items) -> {
            var list = items.map(this::mapToAchievementConditionResponse).stream().toList();
            return new PaginationWrapper.Builder<List<AchievementConditionResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    private AchievementResponse mapToAchievementResponse(AchievementEntity entity) {
        return AchievementResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .icon(entity.getIcon())
                .isActivated(entity.getIsActivated())
                .build();
    }

    private Predicate getAchievementPredicate(Map<String, QueryFieldWrapper> param, Root<AchievementEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = achievementRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private Predicate getAchievementConditionPredicate(Map<String, QueryFieldWrapper> param, Root<AchievementConditionEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = achievementConditionRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private AchievementConditionResponse mapToAchievementConditionResponse(AchievementConditionEntity entity) {
        return AchievementConditionResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .threshold(entity.getThreshold())
                .periodDays(entity.getPeriodDays())
                .build();
    }
}
