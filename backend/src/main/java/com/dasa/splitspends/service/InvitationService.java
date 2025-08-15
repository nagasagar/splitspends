package com.dasa.splitspends.service;

import java.util.List;

import com.dasa.splitspends.entity.Invitation;

public interface InvitationService {

    /**
     * Send an invitation to join a group.
     */
    Invitation sendInvitation(Long groupId, String email, Long invitedByUserId, String personalMessage);

    /**
     * Accept an invitation using token.
     */
    Invitation acceptInvitation(String token, Long acceptingUserId);

    /**
     * Decline an invitation using token.
     */
    Invitation declineInvitation(String token, Long decliningUserId, String reason);

    /**
     * Get all invitations for a group.
     */
    List<Invitation> getGroupInvitations(Long groupId);

    /**
     * Get all invitations for a user.
     */
    List<Invitation> getUserInvitations(Long userId);

    /**
     * Get pending invitations for a user.
     */
    List<Invitation> getPendingInvitations(Long userId);

    /**
     * Get invitation by token.
     */
    Invitation getInvitationByToken(String token);

    /**
     * Cancel a pending invitation.
     */
    void cancelInvitation(Long invitationId, Long cancelledByUserId);

    /**
     * Mark expired invitations.
     */
    void expireOldInvitations();

    /**
     * Send reminder emails for pending invitations.
     */
    void sendInvitationReminders();

    /**
     * Check if invitation token is valid.
     */
    boolean isInvitationValid(String token);

    /**
     * Get invitations sent by a user.
     */
    List<Invitation> getInvitationsSentByUser(Long userId);
}