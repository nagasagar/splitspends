package com.dasa.splitspends.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_recipient", columnList = "recipient_id"),
        @Index(name = "idx_notification_type", columnList = "type"),
        @Index(name = "idx_notification_read_status", columnList = "is_read"),
        @Index(name = "idx_notification_created_at", columnList = "created_at"),
        @Index(name = "idx_notification_group", columnList = "group_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== NOTIFICATION RECIPIENT ==========

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // ========== NOTIFICATION CONTENT ==========

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @NotBlank(message = "Notification title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Notification message is required")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    // ========== NOTIFICATION CONTEXT ==========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by")
    private User triggeredBy; // User who caused this notification

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group; // Related group (if applicable)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private Expense expense; // Related expense (if applicable)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settle_up_id")
    private SettleUp settleUp; // Related settlement (if applicable)

    // Store additional context data as JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "TEXT")
    private Map<String, Object> metadata;

    // ========== NOTIFICATION STATUS ==========

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 10)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Action URL for clickable notifications
    @Column(name = "action_url", length = 500)
    private String actionUrl;

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Mark notification as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Mark notification as unread
     */
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }

    /**
     * Check if notification is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if notification is high priority
     */
    public boolean isHighPriority() {
        return Priority.HIGH.equals(priority) || Priority.URGENT.equals(priority);
    }

    /**
     * Get notification age in hours
     */
    public long getAgeInHours() {
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toHours();
    }

    /**
     * Get formatted notification for display
     */
    public String getFormattedMessage() {
        if (triggeredBy != null) {
            return message.replace("{user}", triggeredBy.getName());
        }
        return message;
    }

    // ========== STATIC FACTORY METHODS ==========

    /**
     * Create expense added notification
     */
    public static Notification createExpenseAddedNotification(User recipient, User triggeredBy,
            Expense expense, Group group) {
        return Notification.builder()
                .recipient(recipient)
                .triggeredBy(triggeredBy)
                .expense(expense)
                .group(group)
                .type(NotificationType.EXPENSE_ADDED)
                .title("New Expense Added")
                .message(String.format("%s added expense '%s' to %s",
                        triggeredBy.getName(), expense.getDescription(), group.getName()))
                .priority(Priority.NORMAL)
                .build();
    }

    /**
     * Create settlement request notification
     */
    public static Notification createSettlementRequestNotification(User recipient, User triggeredBy,
            SettleUp settleUp, Group group) {
        return Notification.builder()
                .recipient(recipient)
                .triggeredBy(triggeredBy)
                .settleUp(settleUp)
                .group(group)
                .type(NotificationType.SETTLEMENT_REQUEST)
                .title("Settlement Request")
                .message(String.format("%s wants to settle up %s with you",
                        triggeredBy.getName(), settleUp.getFormattedAmount()))
                .priority(Priority.HIGH)
                .build();
    }

    /**
     * Create group invitation notification
     */
    public static Notification createGroupInvitationNotification(User recipient, User triggeredBy,
            Group group) {
        return Notification.builder()
                .recipient(recipient)
                .triggeredBy(triggeredBy)
                .group(group)
                .type(NotificationType.GROUP_INVITATION)
                .title("Group Invitation")
                .message(String.format("%s invited you to join '%s'",
                        triggeredBy.getName(), group.getName()))
                .priority(Priority.HIGH)
                .build();
    }

    // ========== ENUMS ==========

    public enum NotificationType {

        // Expense notifications
        EXPENSE_ADDED("New expense added"),
        EXPENSE_UPDATED("Expense updated"),
        EXPENSE_DELETED("Expense deleted"),

        // Settlement notifications
        SETTLEMENT_REQUEST("Settlement request"),
        SETTLEMENT_REQUESTED("Settlement requested"),
        SETTLEMENT_CONFIRMED("Settlement confirmed"),
        SETTLEMENT_REMINDER("Settlement reminder"),
        SETTLEMENT_COMPLETED("Settlement completed"),
        SETTLEMENT_REJECTED("Settlement rejected"),

        // Group notifications
        GROUP_INVITATION("Group invitation"),
        GROUP_MEMBER_ADDED("Group member added"),
        MEMBER_ADDED("Member added to group"),
        MEMBER_REMOVED("Member removed from group"),
        GROUP_UPDATED("Group updated"),

        // Balance notifications
        BALANCE_REMINDER("Balance reminder"),
        PAYMENT_DUE("Payment due"),

        // System notifications
        SYSTEM_UPDATE("System update"),
        WELCOME("Welcome message");

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum Priority {
        LOW("Low priority"),
        NORMAL("Normal priority"),
        HIGH("High priority"),
        URGENT("Urgent");

        private final String description;

        Priority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
