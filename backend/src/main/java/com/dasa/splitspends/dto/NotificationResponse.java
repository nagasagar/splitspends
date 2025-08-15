package com.dasa.splitspends.dto;

import java.time.LocalDateTime;

import com.dasa.splitspends.entity.Notification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private UserResponse recipient;
    private String title;
    private String message;
    private Notification.NotificationType type;
    private Notification.Priority priority;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private LocalDateTime expiresAt;
    private String metadata;
    
    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipient(notification.getRecipient() != null ? UserResponse.fromEntity(notification.getRecipient()) : null)
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .priority(notification.getPriority())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .expiresAt(notification.getExpiresAt())
                .metadata(notification.getMetadata())
                .build();
    }
}