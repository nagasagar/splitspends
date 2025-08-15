package com.dasa.splitspends.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dasa.splitspends.dto.NotificationResponse;
import com.dasa.splitspends.entity.Notification;
import com.dasa.splitspends.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        List<Notification> notifications = notificationService.getUserNotifications(userId, unreadOnly);
        List<NotificationResponse> response = notifications.stream()
                .map(NotificationResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<Page<NotificationResponse>> getUserNotificationsPaginated(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getUserNotificationsPaginated(userId, pageable);
        Page<NotificationResponse> response = notifications.map(NotificationResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long notificationId) {
        Notification notification = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(NotificationResponse.fromEntity(notification));
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Integer> markAllAsRead(@PathVariable Long userId) {
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/activity-feed")
    public ResponseEntity<List<NotificationResponse>> getActivityFeedNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "7") int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Notification> notifications = notificationService.getActivityFeedNotifications(userId, since);
        List<NotificationResponse> response = notifications.stream()
                .map(NotificationResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/high-priority")
    public ResponseEntity<List<NotificationResponse>> getHighPriorityNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getHighPriorityNotifications(userId);
        List<NotificationResponse> response = notifications.stream()
                .map(NotificationResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Void> deleteOldNotifications(@RequestParam(defaultValue = "30") int daysOld) {
        notificationService.deleteOldNotifications(daysOld);
        return ResponseEntity.ok().build();
    }
}