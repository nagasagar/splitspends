package com.dasa.splitspends.dto;

import java.time.LocalDateTime;

import com.dasa.splitspends.entity.Notification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        String metadataJson = null;
        if (notification.getMetadata() != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                metadataJson = objectMapper.writeValueAsString(notification.getMetadata());
            } catch (JsonProcessingException e) {
                metadataJson = null;
            }
        }
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipient(notification.getRecipient() != null ? UserResponse.fromEntity(notification.getRecipient())
                        : null)
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .priority(notification.getPriority())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .expiresAt(notification.getExpiresAt())
                .metadata(metadataJson)
                .build();
    }
}