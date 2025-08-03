package com.dasa.splitspends.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.Invitation;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.InvitationRepository;
import com.dasa.splitspends.repository.UserRepository;

@Service
@Transactional
public class InvitationService {

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private NotificationService notificationService;

    // ========== BASIC CRUD OPERATIONS ==========

    /**
     * Send an invitation to join a group
     */
    public Invitation sendInvitation(Group group, String email, String invitedName,
            User invitedBy, String personalMessage) {
        validateInvitationCreation(group, email, invitedBy);

        // Check for existing pending invitations
        if (invitationRepository.hasPendingInvitation(email, group)) {
            throw new IllegalStateException("User already has a pending invitation to this group");
        }

        Invitation invitation = Invitation.builder()
                .group(group)
                .email(email.toLowerCase())
                .invitedName(invitedName)
                .invitedBy(invitedBy)
                .personalMessage(personalMessage)
                .expiresAt(LocalDateTime.now().plusDays(7)) // Default 7 days
                .build();

        Invitation saved = invitationRepository.save(invitation);

        // Log activity
        activityLogService.logInvitationSent(invitedBy, group, saved);

        // Send notification if user exists
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            notificationService.createGroupInvitationNotification(
                    existingUser.get(), invitedBy, group);
        }

        // TODO: Send email notification
        markEmailAsSent(saved);

        return saved;
    }

    /**
     * Get invitation by ID
     */
    @Transactional(readOnly = true)
    public Optional<Invitation> getInvitationById(Long id) {
        return invitationRepository.findById(id);
    }

    /**
     * Get invitation by token
     */
    @Transactional(readOnly = true)
    public Optional<Invitation> getInvitationByToken(String token) {
        return invitationRepository.findByInvitationToken(token);
    }

    /**
     * Accept an invitation
     */
    public Invitation acceptInvitation(String token, User user) {
        Invitation invitation = getInvitationByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        validateInvitationAcceptance(invitation, user);

        invitation.accept(user);
        Invitation accepted = invitationRepository.save(invitation);

        // Log activity
        activityLogService.logInvitationAccepted(user, invitation.getGroup(), accepted);
        activityLogService.logUserJoinedGroup(user, invitation.getGroup());

        // Send notification to group members
        notifyGroupMembersOfNewMember(invitation.getGroup(), user, invitation.getInvitedBy());

        return accepted;
    }

    /**
     * Decline an invitation
     */
    public Invitation declineInvitation(String token, String reason) {
        Invitation invitation = getInvitationByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        validateInvitationDecline(invitation);

        invitation.decline(reason);
        Invitation declined = invitationRepository.save(invitation);

        // Log activity
        activityLogService.logInvitationDeclined(invitation.getEmail(), invitation.getGroup(), declined);

        return declined;
    }

    /**
     * Cancel an invitation
     */
    public Invitation cancelInvitation(Long id, User cancelledBy) {
        Invitation invitation = getInvitationById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        validateInvitationCancellation(invitation, cancelledBy);

        invitation.cancel();
        Invitation cancelled = invitationRepository.save(invitation);

        // Log activity
        activityLogService.logInvitationSent(cancelledBy, invitation.getGroup(), cancelled);

        return cancelled;
    }

    /**
     * Resend an invitation
     */
    public Invitation resendInvitation(Long id, User resentBy) {
        Invitation invitation = getInvitationById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        validateInvitationResend(invitation, resentBy);

        // Extend expiration date
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));

        // Reset reminder count
        invitation.setReminderCount(0);
        invitation.setLastReminderSentAt(null);

        Invitation resent = invitationRepository.save(invitation);

        // TODO: Send email notification again
        markEmailAsSent(resent);

        // Log activity
        activityLogService.logInvitationSent(resentBy, invitation.getGroup(), resent);

        return resent;
    }

    // ========== QUERY OPERATIONS ==========

    /**
     * Get invitations for a group
     */
    @Transactional(readOnly = true)
    public List<Invitation> getInvitationsForGroup(Group group) {
        return invitationRepository.findByGroupOrderByCreatedAtDesc(group);
    }

    /**
     * Get invitations sent by a user
     */
    @Transactional(readOnly = true)
    public Page<Invitation> getInvitationsSentByUser(User user, Pageable pageable) {
        return invitationRepository.findByInvitedByOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * Get pending invitations for an email
     */
    @Transactional(readOnly = true)
    public List<Invitation> getPendingInvitationsForEmail(String email) {
        return invitationRepository.findPendingByEmail(email);
    }

    /**
     * Get valid invitations for an email
     */
    @Transactional(readOnly = true)
    public List<Invitation> getValidInvitationsForEmail(String email) {
        return invitationRepository.findValidByEmail(email);
    }

    /**
     * Get pending invitations for a group
     */
    @Transactional(readOnly = true)
    public List<Invitation> getPendingInvitationsForGroup(Group group) {
        return invitationRepository.findPendingByGroup(group);
    }

    /**
     * Get recent invitations for a group
     */
    @Transactional(readOnly = true)
    public List<Invitation> getRecentInvitationsForGroup(Group group, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return invitationRepository.findRecentByGroup(group, since);
    }

    /**
     * Check if user has pending invitation to group
     */
    @Transactional(readOnly = true)
    public boolean hasPendingInvitation(String email, Group group) {
        return invitationRepository.hasPendingInvitation(email, group);
    }

    // ========== BULK OPERATIONS ==========

    /**
     * Send bulk invitations to multiple emails
     */
    public List<Invitation> sendBulkInvitations(Group group, List<String> emails,
            User invitedBy, String personalMessage) {
        validateBulkInvitationCreation(group, emails, invitedBy);

        List<Invitation> invitations = new java.util.ArrayList<>();

        for (String email : emails) {
            try {
                if (!invitationRepository.hasPendingInvitation(email, group) &&
                        !isUserAlreadyMember(email, group)) {

                    Invitation invitation = sendInvitation(group, email, null, invitedBy, personalMessage);
                    invitations.add(invitation);
                }
            } catch (Exception e) {
                // Log error but continue with other invitations
                System.err.println("Failed to send invitation to " + email + ": " + e.getMessage());
            }
        }

        return invitations;
    }

    /**
     * Cancel all pending invitations for a group
     */
    @Transactional
    public int cancelAllPendingInvitations(Group group, User cancelledBy) {
        validateGroupAdmin(group, cancelledBy);
        return invitationRepository.cancelPendingByGroup(group);
    }

    // ========== REMINDER AND MAINTENANCE OPERATIONS ==========

    /**
     * Process invitation reminders
     */
    @Transactional
    public void processInvitationReminders() {
        LocalDateTime reminderThreshold = LocalDateTime.now().minusHours(24); // 24 hours ago
        List<Invitation> invitationsNeedingReminder = invitationRepository
                .findInvitationsNeedingReminder(reminderThreshold);

        for (Invitation invitation : invitationsNeedingReminder) {
            sendInvitationReminder(invitation);
        }
    }

    /**
     * Send reminder for a specific invitation
     */
    public void sendInvitationReminder(Invitation invitation) {
        if (invitation.canSendReminder()) {
            invitation.recordReminderSent();
            invitationRepository.save(invitation);

            // TODO: Send reminder email

            // Send notification if user exists
            Optional<User> existingUser = userRepository.findByEmail(invitation.getEmail());
            if (existingUser.isPresent()) {
                notificationService.createGroupInvitationNotification(
                        existingUser.get(), invitation.getInvitedBy(), invitation.getGroup());
            }
        }
    }

    /**
     * Mark expired invitations
     */
    @Transactional
    public int markExpiredInvitations() {
        return invitationRepository.markExpiredInvitations();
    }

    /**
     * Clean up old invitations
     */
    @Transactional
    public int cleanupOldInvitations(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        return invitationRepository.deleteOldInvitations(cutoffDate);
    }

    /**
     * Get invitations needing first reminder
     */
    @Transactional(readOnly = true)
    public List<Invitation> getInvitationsNeedingFirstReminder() {
        LocalDateTime reminderThreshold = LocalDateTime.now().minusDays(2); // 2 days old
        return invitationRepository.findInvitationsForFirstReminder(reminderThreshold);
    }

    // ========== STATISTICS AND ANALYTICS ==========

    /**
     * Get invitation statistics for a user
     */
    @Transactional(readOnly = true)
    public InvitationStats getInvitationStatsForUser(User user) {
        List<Object[]> stats = invitationRepository.getInvitationStatsByUser(user);

        long sent = 0, accepted = 0, declined = 0, pending = 0;

        for (Object[] stat : stats) {
            Invitation.InvitationStatus status = (Invitation.InvitationStatus) stat[0];
            Long count = (Long) stat[1];

            switch (status) {
                case PENDING -> pending = count;
                case ACCEPTED -> accepted = count;
                case DECLINED -> declined = count;
                default -> {
                }
            }
            sent += count;
        }

        return new InvitationStats(sent, accepted, declined, pending);
    }

    /**
     * Get acceptance rate for a group
     */
    @Transactional(readOnly = true)
    public double getAcceptanceRateForGroup(Group group) {
        Double rate = invitationRepository.getAcceptanceRateByGroup(group);
        return rate != null ? rate : 0.0;
    }

    // ========== EMAIL OPERATIONS ==========

    /**
     * Mark invitation email as sent
     */
    private void markEmailAsSent(Invitation invitation) {
        invitation.markEmailSent();
        invitationRepository.save(invitation);
    }

    /**
     * Get invitations with unsent emails
     */
    @Transactional(readOnly = true)
    public List<Invitation> getUnsentInvitations() {
        return invitationRepository.findUnsentInvitations();
    }

    // ========== VALIDATION METHODS ==========

    private void validateInvitationCreation(Group group, String email, User invitedBy) {
        if (group == null || !group.isActive()) {
            throw new IllegalArgumentException("Invalid or inactive group");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (!group.canUserInvite(invitedBy)) {
            throw new IllegalArgumentException("User does not have permission to invite to this group");
        }

        if (isUserAlreadyMember(email, group)) {
            throw new IllegalArgumentException("User is already a member of this group");
        }
    }

    private void validateBulkInvitationCreation(Group group, List<String> emails, User invitedBy) {
        if (emails == null || emails.isEmpty()) {
            throw new IllegalArgumentException("Email list cannot be empty");
        }

        if (emails.size() > 50) {
            throw new IllegalArgumentException("Cannot send more than 50 invitations at once");
        }

        validateInvitationCreation(group, emails.get(0), invitedBy); // Basic validation
    }

    private void validateInvitationAcceptance(Invitation invitation, User user) {
        if (!invitation.isValid()) {
            throw new IllegalStateException("Invitation is not valid (expired or already processed)");
        }

        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("Invitation email does not match user email");
        }

        if (isUserAlreadyMember(user.getEmail(), invitation.getGroup())) {
            throw new IllegalStateException("User is already a member of this group");
        }
    }

    private void validateInvitationDecline(Invitation invitation) {
        if (!invitation.isValid()) {
            throw new IllegalStateException("Invitation is not valid (expired or already processed)");
        }
    }

    private void validateInvitationCancellation(Invitation invitation, User cancelledBy) {
        if (!invitation.getStatus().equals(Invitation.InvitationStatus.PENDING)) {
            throw new IllegalStateException("Only pending invitations can be cancelled");
        }

        if (!invitation.getInvitedBy().equals(cancelledBy) &&
                !invitation.getGroup().isAdmin(cancelledBy)) {
            throw new IllegalArgumentException("Only invitation sender or group admin can cancel invitation");
        }
    }

    private void validateInvitationResend(Invitation invitation, User resentBy) {
        if (!invitation.getStatus().equals(Invitation.InvitationStatus.PENDING)) {
            throw new IllegalStateException("Only pending invitations can be resent");
        }

        if (!invitation.getInvitedBy().equals(resentBy) &&
                !invitation.getGroup().isAdmin(resentBy)) {
            throw new IllegalArgumentException("Only invitation sender or group admin can resend invitation");
        }
    }

    private void validateGroupAdmin(Group group, User user) {
        if (!group.isAdmin(user)) {
            throw new IllegalArgumentException("User must be a group admin to perform this action");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isUserAlreadyMember(String email, Group group) {
        return group.getMembers().stream()
                .anyMatch(member -> email.equalsIgnoreCase(member.getEmail()));
    }

    private void notifyGroupMembersOfNewMember(Group group, User newMember, User invitedBy) {
        for (User member : group.getMembers()) {
            if (!member.equals(newMember) && !member.equals(invitedBy)) {
                notificationService.createMemberAddedNotification(member, invitedBy, newMember, group);
            }
        }
    }

    // ========== HELPER CLASSES ==========

    public static class InvitationStats {
        private final long sent;
        private final long accepted;
        private final long declined;
        private final long pending;

        public InvitationStats(long sent, long accepted, long declined, long pending) {
            this.sent = sent;
            this.accepted = accepted;
            this.declined = declined;
            this.pending = pending;
        }

        public long getSent() {
            return sent;
        }

        public long getAccepted() {
            return accepted;
        }

        public long getDeclined() {
            return declined;
        }

        public long getPending() {
            return pending;
        }

        public double getAcceptanceRate() {
            return sent > 0 ? (double) accepted / sent * 100 : 0.0;
        }
    }
}
