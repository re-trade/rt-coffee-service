package org.retrade.feedback_notification.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.feedback_notification.model.dto.NotificationRequest;
import org.retrade.feedback_notification.model.dto.NotificationResponse;
import org.retrade.feedback_notification.service.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<ResponseObject<NotificationResponse>> makeNotifications(@RequestBody NotificationRequest notificationResponse) {
        var notification = notificationService.makeNotification(notificationResponse);
        return ResponseEntity.ok(
                new ResponseObject.Builder<NotificationResponse>()
                        .success(true)
                        .code("SUCCESS")
                        .content(notification)
                        .messages("Notification init successfully")
                        .build()
        );
    }

    @PatchMapping("{id}/read")
    public ResponseEntity<ResponseObject<Void>> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(
                new ResponseObject.Builder<Void>()
                        .success(true)
                        .code("SUCCESS")
                        .messages("Notification mark successfully")
                        .build()
        );
    }

    @PostMapping("test")
    public ResponseEntity<ResponseObject<Void>> testNotification () {
        notificationService.testGlobalNotification();
        return ResponseEntity.ok(
                new ResponseObject.Builder<Void>()
                        .success(true)
                        .code("SUCCESS")
                        .messages("Notification test successfully")
                        .build()
        );
    }
}
