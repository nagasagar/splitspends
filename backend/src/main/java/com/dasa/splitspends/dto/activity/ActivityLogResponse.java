package com.dasa.splitspends.dto.activity;

import java.time.LocalDateTime;

import com.dasa.splitspends.entity.ActivityLog;
import com.dasa.splitspends.entity.ActivityLog.Action;
import com.dasa.splitspends.entity.ActivityLog.EntityType;

public class ActivityLogResponse {
    private Long id;
    private Action action;
    private EntityType entityType;
    private Long entityId;
    private String description;
    private String details;
    private Long userId;
    private String userName;
    private Long groupId;
    private String groupName;
    private String targetUser;
    private LocalDateTime createdAt;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String metadata;

    // Constructors
    public ActivityLogResponse() {
    }

    public ActivityLogResponse(Long id, Action action, EntityType entityType, Long entityId,
            String description, String details, Long userId, String userName,
            Long groupId, String groupName, String targetUser, LocalDateTime createdAt,
            String ipAddress, String userAgent, String sessionId, String metadata) {
        this.id = id;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
        this.details = details;
        this.userId = userId;
        this.userName = userName;
        this.groupId = groupId;
        this.groupName = groupName;
        this.targetUser = targetUser;
        this.createdAt = createdAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.sessionId = sessionId;
        this.metadata = metadata != null ? metadata.toString() : null;
    }

    public static ActivityLogResponse fromEntity(ActivityLog activityLog) {
        return new ActivityLogResponse(
                activityLog.getId(),
                activityLog.getAction(),
                activityLog.getEntityType(),
                activityLog.getEntityId(),
                activityLog.getDescription(),
                activityLog.getDetails(),
                activityLog.getUser().getId(),
                activityLog.getUser().getName(),
                activityLog.getGroup() != null ? activityLog.getGroup().getId() : null,
                activityLog.getGroup() != null ? activityLog.getGroup().getName() : null,
                activityLog.getTargetUser(),
                activityLog.getCreatedAt(),
                activityLog.getIpAddress(),
                activityLog.getUserAgent(),
                activityLog.getSessionId(),
                activityLog.getMetadata() != null ? activityLog.getMetadata().toString() : null);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
