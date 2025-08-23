package com.dasa.splitspends.dto;

import java.time.LocalDateTime;

import com.dasa.splitspends.dto.group.GroupResponse;
import com.dasa.splitspends.entity.Invitation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvitationResponse {
    private Long id;
    private GroupResponse group;
    private String email;
    private String invitedName;
    private UserResponse invitedBy;
    private String personalMessage;
    private String invitationToken;
    private Invitation.InvitationStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime declinedAt;
    private LocalDateTime cancelledAt;
    private UserResponse acceptedBy;
    private String declineReason;
    private Integer reminderCount;
    private LocalDateTime lastEmailSentAt;

    public static InvitationResponse fromEntity(Invitation invitation) {
        // Compute lastEmailSentAt as the latest of emailSentAt and lastReminderSentAt
        LocalDateTime lastEmailSentAt = null;
        if (invitation.getEmailSentAt() != null && invitation.getLastReminderSentAt() != null) {
            lastEmailSentAt = invitation.getEmailSentAt().isAfter(invitation.getLastReminderSentAt())
                    ? invitation.getEmailSentAt()
                    : invitation.getLastReminderSentAt();
        } else if (invitation.getEmailSentAt() != null) {
            lastEmailSentAt = invitation.getEmailSentAt();
        } else if (invitation.getLastReminderSentAt() != null) {
            lastEmailSentAt = invitation.getLastReminderSentAt();
        }

        return InvitationResponse.builder()
                .id(invitation.getId())
                .group(invitation.getGroup() != null ? GroupResponse.fromEntity(invitation.getGroup()) : null)
                .email(invitation.getEmail())
                .invitedName(invitation.getInvitedName())
                .invitedBy(
                        invitation.getInvitedBy() != null ? UserResponse.fromEntity(invitation.getInvitedBy()) : null)
                .personalMessage(invitation.getPersonalMessage())
                .invitationToken(invitation.getInvitationToken())
                .status(invitation.getStatus())
                .expiresAt(invitation.getExpiresAt())
                .createdAt(invitation.getCreatedAt())
                .acceptedAt(invitation.getAcceptedAt())
                .declinedAt(invitation.getDeclinedAt())
                .cancelledAt(invitation.getCancelledAt())
                .acceptedBy(
                        invitation.getAcceptedBy() != null ? UserResponse.fromEntity(invitation.getAcceptedBy()) : null)
                .declineReason(invitation.getDeclineReason())
                .reminderCount(invitation.getReminderCount())
                .lastEmailSentAt(lastEmailSentAt)
                .build();
    }
}