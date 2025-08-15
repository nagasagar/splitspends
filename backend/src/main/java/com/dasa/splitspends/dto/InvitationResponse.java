package com.dasa.splitspends.dto;

import java.time.LocalDateTime;

import com.dasa.splitspends.entity.Invitation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvitationResponse {
    private Long id;
    private GroupResponse group;
    private String email;
    private UserResponse invitedUser;
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
        return InvitationResponse.builder()
                .id(invitation.getId())
                .group(invitation.getGroup() != null ? GroupResponse.fromEntity(invitation.getGroup()) : null)
                .email(invitation.getEmail())
                .invitedUser(invitation.getInvitedUser() != null ? UserResponse.fromEntity(invitation.getInvitedUser()) : null)
                .invitedBy(invitation.getInvitedBy() != null ? UserResponse.fromEntity(invitation.getInvitedBy()) : null)
                .personalMessage(invitation.getPersonalMessage())
                .invitationToken(invitation.getInvitationToken())
                .status(invitation.getStatus())
                .expiresAt(invitation.getExpiresAt())
                .createdAt(invitation.getCreatedAt())
                .acceptedAt(invitation.getAcceptedAt())
                .declinedAt(invitation.getDeclinedAt())
                .cancelledAt(invitation.getCancelledAt())
                .acceptedBy(invitation.getAcceptedBy() != null ? UserResponse.fromEntity(invitation.getAcceptedBy()) : null)
                .declineReason(invitation.getDeclineReason())
                .reminderCount(invitation.getReminderCount())
                .lastEmailSentAt(invitation.getLastEmailSentAt())
                .build();
    }
}