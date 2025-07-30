package org.retrade.achievement.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.retrade.achievement.client.TokenServiceClient;
import org.retrade.achievement.model.constant.JwtTokenType;
import org.retrade.achievement.model.dto.response.SellerAchievementResponse;
import org.retrade.achievement.model.entity.SellerAchievementEntity;
import org.retrade.achievement.repository.SellerAchievementRepository;
import org.retrade.achievement.service.SellerAchievementService;
import org.retrade.achievement.util.CookieUtils;
import org.retrade.achievement.util.TokenUtils;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.AuthException;
import org.retrade.proto.authentication.GetSellerProfileResponse;
import org.retrade.proto.authentication.TokenType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SellerAchievementServiceImpl implements SellerAchievementService {
    private final SellerAchievementRepository sellerAchievementRepository;
    private final TokenServiceClient tokenServiceClient;

    @Override
    public PaginationWrapper<List<SellerAchievementResponse>> getSellerAchievementCompletedBySellerId(String sellerId, QueryWrapper queryWrapper) {
        return sellerAchievementRepository.query(queryWrapper, (param) -> ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("sellerId"), sellerId));
            predicates.add(criteriaBuilder.equal(root.get("achieved"), true));
            return getPredicate(param, root, criteriaBuilder, predicates);
        }), (items) -> {
            var list = items.map(this::mapToSellerAchievementResponse).stream().toList();
            return new PaginationWrapper.Builder<List<SellerAchievementResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    public PaginationWrapper<List<SellerAchievementResponse>> getSellerAchievementCompleted(HttpServletRequest httpServletRequest, QueryWrapper queryWrapper) {
        String accessToken = TokenUtils.getTokenFromHeader(httpServletRequest);
        GetSellerProfileResponse sellerInfo = resolveSellerProfile(httpServletRequest);
        return sellerAchievementRepository.query(queryWrapper, (param) -> ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("sellerId"), sellerInfo.getUserInfo().getSellerId()));
            predicates.add(criteriaBuilder.equal(root.get("achieved"), true));
            return getPredicate(param, root, criteriaBuilder, predicates);
        }), (items) -> {
            var list = items.map(this::mapToSellerAchievementResponse).stream().toList();
            return new PaginationWrapper.Builder<List<SellerAchievementResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    private GetSellerProfileResponse resolveSellerProfile(HttpServletRequest request) {
        String accessToken = TokenUtils.getTokenFromHeader(request);
        GetSellerProfileResponse sellerInfo = null;

        if (accessToken != null && !accessToken.isEmpty()) {
            sellerInfo = tokenServiceClient.getSellerProfileByToken(accessToken, TokenType.ACCESS_TOKEN);
        } else {
            var cookieMap = CookieUtils.getCookieMap(request);
            if (cookieMap.containsKey(JwtTokenType.ACCESS_TOKEN)) {
                sellerInfo = tokenServiceClient.getSellerProfileByToken(
                        cookieMap.get(JwtTokenType.ACCESS_TOKEN).getValue(),
                        TokenType.ACCESS_TOKEN
                );
            } else if (cookieMap.containsKey(JwtTokenType.REFRESH_TOKEN)) {
                sellerInfo = tokenServiceClient.getSellerProfileByToken(
                        cookieMap.get(JwtTokenType.REFRESH_TOKEN).getValue(),
                        TokenType.REFRESH_TOKEN
                );
            }
        }

        if (sellerInfo == null || !sellerInfo.getIsValid()) {
            throw new AuthException("No valid JWT token found in request");
        }

        return sellerInfo;
    }


    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<SellerAchievementEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = sellerAchievementRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }


    private SellerAchievementResponse mapToSellerAchievementResponse(SellerAchievementEntity entity) {
        var achievement = entity.getAchievement();
        return SellerAchievementResponse.builder()
                .id(entity.getId())
                .code(achievement.getCode())
                .name(achievement.getName())
                .description(achievement.getDescription())
                .icon(achievement.getIcon())
                .progress(entity.getProgress())
                .achievedAt(String.valueOf(entity.getAchievedAt()))
                .achieved( entity.getAchieved())
                .build();
    }
}
