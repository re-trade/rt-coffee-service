package org.retrade.feedback_notification.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.feedback_notification.model.dto.NotificationResponse;
import org.retrade.feedback_notification.model.entity.NotificationEntity;
import org.retrade.feedback_notification.repository.NotificationRepository;
import org.retrade.feedback_notification.service.NotificationService;
import org.retrade.feedback_notification.service.WebSocketService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final WebSocketService webSocketService;

    @Override
    public PaginationWrapper<List<NotificationResponse>> getNotifications(QueryWrapper queryWrapper) {
        return notificationRepository.query(queryWrapper, (param) -> ((root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();
            return getPredicate(param, root, criteriaBuilder, predicates);
        }), (items) -> {
            var response = items.stream().map(this::wrapToNotificationResponse).toList();
            return new PaginationWrapper.Builder<List<NotificationResponse>>()
                    .setPaginationInfo(items)
                    .setData(response)
                    .build();
        });
    }

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<NotificationEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = notificationRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private NotificationResponse wrapToNotificationResponse(NotificationEntity notificationEntity) {
        return NotificationResponse.builder()
                .id(notificationEntity.getId())
                .type(notificationEntity.getType())
                .title(notificationEntity.getTitle())
                .content(notificationEntity.getContent())
                .read(notificationEntity.isRead())
                .createdDate(notificationEntity.getCreatedDate().toLocalDateTime())
                .build();
    }
}
