package org.retrade.feedback_notification.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.feedback_notification.client.TokenServiceClient;
import org.retrade.feedback_notification.model.constant.NotificationTypeCode;
import org.retrade.feedback_notification.model.dto.NotificationRequest;
import org.retrade.feedback_notification.model.dto.NotificationResponse;
import org.retrade.feedback_notification.model.entity.AccountEntity;
import org.retrade.feedback_notification.model.entity.NotificationEntity;
import org.retrade.feedback_notification.model.message.SocketNotificationMessage;
import org.retrade.feedback_notification.repository.AccountRepository;
import org.retrade.feedback_notification.repository.NotificationRepository;
import org.retrade.feedback_notification.service.NotificationService;
import org.retrade.feedback_notification.service.WebSocketService;
import org.retrade.feedback_notification.util.AuthUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final AuthUtils authUtils;
    private final WebSocketService webSocketService;
    private final AccountRepository accountRepository;
    private final TokenServiceClient tokenServiceClient;

    @Override
    public PaginationWrapper<List<NotificationResponse>> getNotifications(QueryWrapper queryWrapper) {
        var account = authUtils.getUserAccountFromAuthentication();
        return notificationRepository.query(queryWrapper, (param) -> ((root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(criteriaBuilder.equal(root.get("account"), account));
            return getPredicate(param, root, criteriaBuilder, predicates);
        }), (items) -> {
            var response = items.stream().map(this::wrapToNotificationResponse).toList();
            return new PaginationWrapper.Builder<List<NotificationResponse>>()
                    .setPaginationInfo(items)
                    .setData(response)
                    .build();
        });
    }


    @Override
    @Transactional(rollbackFor = {ValidationException.class, ActionFailedException.class, Exception.class})
    public void makeUserNotificationRead(SocketNotificationMessage message) {
        var account = accountRepository.findByAccountId(message.getAccountId()).orElseGet(() -> {
            var result = tokenServiceClient.getAccountInfoById(message.getAccountId());
            if (!result.getIsValid()) {
                throw new ActionFailedException("Failed");
            }
            var userInfo = result.getUserInfo();
            var roleMapper = userInfo.getRolesList().stream().map(String::toUpperCase).collect(Collectors.toSet());
            var tempAccount = AccountEntity.builder()
                    .accountId(userInfo.getAccountId())
                    .username(userInfo.getUsername())
                    .roles(roleMapper)
                    .build();
            try {
                return accountRepository.save(tempAccount);
            } catch (Exception e) {
                throw new ActionFailedException("Failed to mark notification as read", e);
            }
        });
        var notification = NotificationEntity.builder()
                .message(message.getTitle())
                .content(message.getContent())
                .title(message.getTitle())
                .read(false)
                .type(message.getType())
                .build();
        notification.setAccount(account);
        try {
            var result = notificationRepository.save(notification);
            webSocketService.sentToUser(message.getAccountId(), wrapToNotificationResponse(result));
        } catch (Exception e) {
            throw new ActionFailedException("Failed to mark notification as read", e);
        }
    }

    @Override
    public NotificationResponse makeNotification(NotificationRequest notificationRequest) {
        var notification = NotificationEntity.builder()
                .message(notificationRequest.getMessage())
                .content(notificationRequest.getContent())
                .title(notificationRequest.getTitle())
                .read(false)
                .type(NotificationTypeCode.SYSTEM)
                .build();
        try {
            var result = notificationRepository.save(notification);
            var response = wrapToNotificationResponse(result);
            webSocketService.sentToAll(response);
            return response;
        } catch (Exception e) {
            throw new ActionFailedException("Failed to create notification", e);
        }
    }

    @Override
    public void testGlobalNotification() {
        try {
            var response = NotificationResponse.builder()
                    .id("test")
                    .type(NotificationTypeCode.ALERT)
                    .title("Testing")
                    .content("Testing")
                    .read(false)
                    .createdDate(LocalDateTime.now())
                    .build();
            webSocketService.sentToAll(response);
        } catch (Exception e) {
            throw new ActionFailedException("Failed to create notification", e);
        }
    }

    @Override
    public void markAsRead(String id) {
        var account = authUtils.getUserAccountFromAuthentication();
        var result = notificationRepository.findByIdAndAccount(id, account).orElseThrow(() -> new ValidationException("Notification not found"));
        result.setRead(true);
        try {
            notificationRepository.save(result);
        } catch (Exception e) {
            throw new ActionFailedException("Failed to mark notification as read", e);
        }
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
