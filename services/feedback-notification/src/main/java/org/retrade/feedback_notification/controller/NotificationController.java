package org.retrade.feedback_notification.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.feedback_notification.model.dto.NotificationResponse;
import org.retrade.feedback_notification.service.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/me")
    public ResponseEntity<ResponseObject<List<NotificationResponse>>> getUserNotification(@PageableDefault Pageable pageable, @RequestParam(required = false, name = "q") String query) {
        var notifications = notificationService.getNotifications(QueryWrapper.builder()
                        .wrapSort(pageable)
                        .search(query)
                .build());
        return ResponseEntity.ok(
          new ResponseObject.Builder<List<NotificationResponse>>()
                  .success(true)
                  .code("SUCCESS")
                  .unwrapPaginationWrapper(notifications)
                  .messages("Notification retrieved successfully")
                  .build()
        );
    }
}
