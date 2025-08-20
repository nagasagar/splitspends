
package com.dasa.splitspends.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invitations", indexes = {
        @Index(name = "idx_invitation_group", columnList = "group_id"),
        @Index(name = "idx_invitation_invited_by", columnList = "invited_by"),
        @Index(name = "idx_invitation_status", columnList = "status"),
        @Index(name = "idx_invitation_email", columnList = "email"),
        @Index(name = "idx_invitation_token", columnList = "invitation_token"),
        @Index(name = "idx_invitation_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== INVITATION DETAILS ==========

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnore
    private Group group;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by", nullable = false)
    @JsonIgnore
    private User invitedBy;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Size(max = 100, message = "Invited name cannot exceed 100 characters")
    @Column(name = "invited_name", length = 100)
    private String invitedName; // Optional name for the invitee

    // ========== INVITATION TOKEN & SECURITY ==========

    @NotBlank
    @Column(name = "invitation_token", nullable = false, unique = true, length = 64)
    private String invitationToken;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    // ========== INVITATION RESPONSE ==========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_by")
    @JsonIgnore
    private User acceptedBy; // User who accepted (if they had an account)

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "declined_at")
    private LocalDateTime declinedAt;

    @Size(max = 500, message = "Decline reason cannot exceed 500 characters")
    @Column(name = "decline_reason", length = 500)
    private String declineReason;

    // ========== INVITATION MESSAGE ==========

    @Size(max = 500, message = "Personal message cannot exceed 500 characters")
    @Column(name = "personal_message", length = 500)
    private String personalMessage;

    // Track email sending
    @Column(name = "email_sent", nullable = false)
    @Builder.Default
    private Boolean emailSent = false;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @Column(name = "reminder_count", nullable = false)
    @Builder.Default
    private Integer reminderCount = 0;

    @Column(name = "last_reminder_sent_at")
    private LocalDateTime lastReminderSentAt;

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Accept the invitation
     */
    public void accept(User user) {
        this.status = InvitationStatus.ACCEPTED;
        this.acceptedBy = user;
        this.acceptedAt = LocalDateTime.now();

        // Add user to the group
        group.addMember(user);
    }

    /**
     * Decline the invitation
     */
    public void decline(String reason) {
        this.status = InvitationStatus.DECLINED;
        this.declinedAt = LocalDateTime.now();
        this.declineReason = reason;
    }

    /**
     * Cancel the invitation
     */
    public void cancel() {
        this.status = InvitationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * Mark invitation as expired
     */
    public void expire() {
        this.status = InvitationStatus.EXPIRED;
    }

    /**
     * Check if invitation is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt) ||
                InvitationStatus.EXPIRED.equals(status);
    }

    /**
     * Check if invitation is still valid (pending and not expired)
     */
    public boolean isValid() {
        return InvitationStatus.PENDING.equals(status) && !isExpired();
    }

    /**
     * Mark email as sent
     */
    public void markEmailSent() {
        this.emailSent = true;
        this.emailSentAt = LocalDateTime.now();
    }

    /**
     * Record reminder sent
     */
    public void recordReminderSent() {
        this.reminderCount++;
        this.lastReminderSentAt = LocalDateTime.now();
    }

    /**
     * Check if reminder can be sent (max 3 reminders, at least 24 hours apart)
     */
    public boolean canSendReminder() {
        if (reminderCount >= 3 || !isValid()) {
            return false;
        }

        if (lastReminderSentAt == null) {
            return true;
        }

        return LocalDateTime.now().isAfter(lastReminderSentAt.plusHours(24));
    }

    /**
     * Get invitation URL
     */
    public String getInvitationUrl(String baseUrl) {
        return String.format("%s/invite/%s", baseUrl, invitationToken);
    }

    /**
     * Get time until expiration in hours
     */
    public long getHoursUntilExpiration() {
        if (isExpired())
            return 0;
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toHours();
    }

    /**
     * Get display name for invitee
     */
    public String getInviteeDisplayName() {
        return invitedName != null ? invitedName : email;
    }

    // ========== VALIDATION METHODS ==========

    @PrePersist
    private void validateInvitation() {
        // Generate token if not provided
        if (invitationToken == null || invitationToken.isEmpty()) {
            invitationToken = generateInvitationToken();
        }

        // Set expiration if not provided (default 7 days)
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusDays(7);
        }

        // Validate that inviter can invite to this group
        if (group != null && invitedBy != null && !group.canUserInvite(invitedBy)) {
            throw new IllegalStateException("User does not have permission to invite to this group");
        }

        // Check if user is already a member
        if (group != null && group.getMembers() != null) {
            boolean alreadyMember = group.getMembers().stream()
                    .anyMatch(member -> email.equalsIgnoreCase(member.getEmail()));
            if (alreadyMember) {
                throw new IllegalStateException("User is already a member of this group");
            }
        }
    }

    /**
     * Generate a secure invitation token
     */
    private String generateInvitationToken() {
        return java.util.UUID.randomUUID().toString().replace("-", "") +
                System.currentTimeMillis();
    }

    // ========== ENUMS ==========

    public enum InvitationStatus {
        PENDING("Invitation sent, awaiting response"),
        ACCEPTED("Invitation accepted"),
        DECLINED("Invitation declined"),
        EXPIRED("Invitation expired"),
        CANCELLED("Invitation cancelled");

        private final String description;

        InvitationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
