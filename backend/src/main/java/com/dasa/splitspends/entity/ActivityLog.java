
package com.dasa.splitspends.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_log_user", columnList = "user_id"),
        @Index(name = "idx_activity_log_group", columnList = "group_id"),
        @Index(name = "idx_activity_log_action", columnList = "action"),
        @Index(name = "idx_activity_log_entity_type", columnList = "entity_type"),
        @Index(name = "idx_activity_log_created_at", columnList = "created_at"),
        @Index(name = "idx_activity_log_entity_id", columnList = "entity_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== ACTIVITY CONTEXT ==========

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user; // User who performed the action

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @JsonIgnore
    private Group group; // Related group (if applicable)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ========== ACTION DETAILS ==========

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private Action action;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 30)
    private EntityType entityType;

    @Column(name = "entity_id")
    private Long entityId; // ID of the affected entity

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    // ========== ADDITIONAL CONTEXT ==========

    @Size(max = 100, message = "Target user cannot exceed 100 characters")
    @Column(name = "target_user", length = 100)
    private String targetUser; // User affected by the action (for member actions)

    @Size(max = 200, message = "Details cannot exceed 200 characters")
    @Column(name = "details", length = 200)
    private String details; // Additional action details

    // Store additional context data as JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "TEXT")
    private Map<String, Object> metadata;

    // ========== TRACKING ==========

    @Column(name = "ip_address", length = 45)
    private String ipAddress; // IPv4 or IPv6 address

    @Column(name = "user_agent", length = 500)
    private String userAgent; // Browser/app information

    @Column(name = "session_id", length = 100)
    private String sessionId; // Session identifier

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Get formatted activity description
     */
    public String getFormattedDescription() {
        if (targetUser != null) {
            return description.replace("{targetUser}", targetUser);
        }
        return description;
    }

    /**
     * Check if activity is recent (within last hour)
     */
    public boolean isRecent() {
        return LocalDateTime.now().minusHours(1).isBefore(createdAt);
    }

    /**
     * Get activity age in minutes
     */
    public long getAgeInMinutes() {
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
    }

    /**
     * Get relative time string
     */
    public String getRelativeTime() {
        long minutes = getAgeInMinutes();

        if (minutes < 1)
            return "just now";
        if (minutes < 60)
            return minutes + " minutes ago";

        long hours = minutes / 60;
        if (hours < 24)
            return hours + " hours ago";

        long days = hours / 24;
        if (days < 30)
            return days + " days ago";

        return createdAt.toLocalDate().toString();
    }

    // ========== STATIC FACTORY METHODS ==========

    /**
     * Log expense creation
     */
    public static ActivityLog logExpenseCreated(User user, Group group, Expense expense) {
        return ActivityLog.builder()
                .user(user)
                .group(group)
                .action(Action.CREATE)
                .entityType(EntityType.EXPENSE)
                .entityId(expense.getId())
                .description(String.format("%s added expense '%s'",
                        user.getName(), expense.getDescription()))
                .build();
    }

    /**
     * Log settlement completion
     */
    public static ActivityLog logSettlementCompleted(User user, Group group, SettleUp settleUp) {
        return ActivityLog.builder()
                .user(user)
                .group(group)
                .action(Action.COMPLETE)
                .entityType(EntityType.SETTLEMENT)
                .entityId(settleUp.getId())
                .description(String.format("%s completed settlement of %s",
                        user.getName(), settleUp.getFormattedAmount()))
                .targetUser(settleUp.getPayee().getName())
                .build();
    }

    /**
     * Log member addition
     */
    public static ActivityLog logMemberAdded(User user, Group group, User newMember) {
        return ActivityLog.builder()
                .user(user)
                .group(group)
                .action(Action.ADD_MEMBER)
                .entityType(EntityType.GROUP)
                .entityId(group.getId())
                .description(String.format("%s added {targetUser} to the group", user.getName()))
                .targetUser(newMember.getName())
                .build();
    }

    /**
     * Log group creation
     */
    public static ActivityLog logGroupCreated(User user, Group group) {
        return ActivityLog.builder()
                .user(user)
                .group(group)
                .action(Action.CREATE)
                .entityType(EntityType.GROUP)
                .entityId(group.getId())
                .description(String.format("%s created group '%s'", user.getName(), group.getName()))
                .build();
    }

    /**
     * Log user login
     */
    public static ActivityLog logUserLogin(User user, String ipAddress, String userAgent) {
        return ActivityLog.builder()
                .user(user)
                .action(Action.LOGIN)
                .entityType(EntityType.USER)
                .entityId(user.getId())
                .description(String.format("%s logged in", user.getName()))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }

    // ========== ENUMS ==========

    public enum Action {
        // CRUD operations
        CREATE("Created"),
        UPDATE("Updated"),
        DELETE("Deleted"),

        // User actions
        LOGIN("Logged in"),
        LOGOUT("Logged out"),
        REGISTER("Registered"),

        // Group actions
        JOIN_GROUP("Joined group"),
        LEAVE_GROUP("Left group"),
        ADD_MEMBER("Added member"),
        REMOVE_MEMBER("Removed member"),
        PROMOTE_ADMIN("Promoted to admin"),
        DEMOTE_ADMIN("Demoted from admin"),

        // Expense actions
        SPLIT_EXPENSE("Split expense"),
        SETTLE_EXPENSE("Settled expense"),

        // Settlement actions
        REQUEST_SETTLEMENT("Requested settlement"),
        ACCEPT_SETTLEMENT("Accepted settlement"),
        REJECT_SETTLEMENT("Rejected settlement"),
        COMPLETE("Completed"),

        // Invitation actions
        SEND_INVITATION("Sent invitation"),
        ACCEPT_INVITATION("Accepted invitation"),
        DECLINE_INVITATION("Declined invitation"),

        // Attachment actions
        UPLOAD_ATTACHMENT("Uploaded attachment"),
        DOWNLOAD_ATTACHMENT("Downloaded attachment"),
        DELETE_ATTACHMENT("Deleted attachment"),

        // Other actions
        ARCHIVE("Archived"),
        RESTORE("Restored"),
        EXPORT("Exported"),
        CANCEL_INVITATION("Cancelled invitation");

        private final String description;

        Action(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum EntityType {
        USER("User"),
        GROUP("Group"),
        EXPENSE("Expense"),
        EXPENSE_SPLIT("Expense Split"),
        SETTLEMENT("Settlement"),
        INVITATION("Invitation"),
        NOTIFICATION("Notification"),
        ATTACHMENT("Attachment");

        private final String description;

        EntityType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
