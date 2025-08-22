package com.dasa.splitspends.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_google_id", columnList = "google_id"),
        @Index(name = "idx_user_status", columnList = "account_status"),
        @Index(name = "idx_user_joined_at", columnList = "joined_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== BASIC PROFILE INFORMATION ==========

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    // ========== AUTHENTICATION & SSO ==========

    @Column(name = "google_id", unique = true, length = 100)
    private String googleId;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "email_verification_sent_at")
    private LocalDateTime emailVerificationSentAt;

    // ========== USER PREFERENCES ==========

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
    @Column(name = "preferred_currency", length = 3, nullable = false)
    @Builder.Default
    private String preferredCurrency = "USD";

    @Column(name = "email_notifications", nullable = false)
    @Builder.Default
    private Boolean emailNotifications = true;

    @Column(name = "push_notifications", nullable = false)
    @Builder.Default
    private Boolean pushNotifications = true;

    @Column(name = "payment_reminders", nullable = false)
    @Builder.Default
    private Boolean paymentReminders = true;

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "UTC";

    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "en";

    // ========== AUDIT FIELDS ==========

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ========== RELATIONSHIPS ==========

    @OneToMany(mappedBy = "paidBy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Expense> expensesPaid;

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Group> groups;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ExpenseSplit> expenseSplits;

    // Notifications received by this user
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Notification> notifications;

    // Activity logs for actions performed by this user
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ActivityLog> activityLogs;

    // Invitations sent by this user
    @OneToMany(mappedBy = "invitedBy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Invitation> invitationsSent;

    // Settlements where this user is the payer
    @OneToMany(mappedBy = "payer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<SettleUp> settlementsAsPayer;

    // Settlements where this user is the payee
    @OneToMany(mappedBy = "payee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<SettleUp> settlementsAsPayee;

    // Attachments uploaded by this user
    @OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Attachment> uploadedAttachments;

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Check if user account is active
     */
    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(accountStatus) && deletedAt == null;
    }

    /**
     * Check if user is verified (email confirmed)
     */
    public boolean isVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    /**
     * Check if user uses Google SSO
     */
    public boolean isGoogleUser() {
        return googleId != null && !googleId.trim().isEmpty();
    }

    /**
     * Get display name (name or email if name not available)
     */
    public String getDisplayName() {
        return (name != null && !name.trim().isEmpty()) ? name : email;
    }

    /**
     * Get username for identification (using name)
     */
    public String getUsername() {
        return this.name;
    }

    /**
     * Soft delete user
     */
    public void softDelete() {
        this.accountStatus = AccountStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Check if user is member of a specific group
     */
    public boolean isMemberOf(Group group) {
        return groups != null && groups.contains(group);
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    /**
     * Get unread notifications count
     */
    public long getUnreadNotificationsCount() {
        if (notifications == null)
            return 0;
        return notifications.stream()
                .filter(notification -> !notification.getIsRead())
                .count();
    }

    /**
     * Get pending settlement requests count
     */
    public long getPendingSettlementsCount() {
        long asPayer = settlementsAsPayer != null ? settlementsAsPayer.stream()
                .filter(settlement -> settlement.getStatus() == SettleUp.SettlementStatus.PENDING)
                .count() : 0;

        long asPayee = settlementsAsPayee != null ? settlementsAsPayee.stream()
                .filter(settlement -> settlement.getStatus() == SettleUp.SettlementStatus.PENDING)
                .count() : 0;

        return asPayer + asPayee;
    }

    /**
     * Get recent activity logs (last 30 days)
     */
    public Set<ActivityLog> getRecentActivityLogs() {
        if (activityLogs == null)
            return new HashSet<>();

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return activityLogs.stream()
                .filter(log -> log.getCreatedAt().isAfter(thirtyDaysAgo))
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Check if user has any outstanding balances
     */
    public boolean hasOutstandingBalances() {
        if (expenseSplits == null)
            return false;
        return expenseSplits.stream()
                .anyMatch(split -> !split.isSettled());
    }

    // ========== VALIDATION METHODS ==========

    @PrePersist
    @PreUpdate
    private void validateUser() {
        // Ensure either password or Google ID exists
        if ((passwordHash == null || passwordHash.trim().isEmpty()) &&
                (googleId == null || googleId.trim().isEmpty())) {
            throw new IllegalStateException("User must have either password or Google ID");
        }

        // Normalize email to lowercase
        if (email != null) {
            this.email = email.toLowerCase().trim();
        }

        // Set joined date if not set
        if (joinedAt == null) {
            this.joinedAt = LocalDateTime.now();
        }
    }

    // ========== ENUMS ==========

    public enum AccountStatus {
        ACTIVE("Active account"),
        SUSPENDED("Temporarily suspended"),
        INACTIVE("Deactivated account"),
        DELETED("Soft deleted"),
        PENDING_VERIFICATION("Awaiting email verification");

        private final String description;

        AccountStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
