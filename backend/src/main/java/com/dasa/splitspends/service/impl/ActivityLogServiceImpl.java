package com.dasa.splitspends.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dasa.splitspends.entity.ActivityLog;
import com.dasa.splitspends.entity.Attachment;
import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.Invitation;
import com.dasa.splitspends.entity.SettleUp;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.ActivityLogRepository;
import com.dasa.splitspends.service.ActivityLogService;

@Service
@Transactional
public class ActivityLogServiceImpl implements ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    // ========== BASIC CRUD OPERATIONS ==========

    /**
     * Create a generic activity log
     */
    @Override
    public ActivityLog createActivityLog(User user, ActivityLog.Action action,
            ActivityLog.EntityType entityType, Long entityId,
            String description, Group group) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .group(group)
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Get activity log by ID
     */
    @Override
    @Transactional(readOnly = true)
    public ActivityLog getActivityLogById(Long id) {
        return activityLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activity log not found"));
    }

    // ========== USER ACTIVITY LOGGING ==========

    /**
     * Log user login
     */
    @Override
    public ActivityLog logUserLogin(User user, String ipAddress, String userAgent, String sessionId) {
        ActivityLog activityLog = ActivityLog.logUserLogin(user, ipAddress, userAgent);
        activityLog.setSessionId(sessionId);
        return activityLogRepository.save(activityLog);
    }

    /**
     * Log user logout
     */
    @Override
    public ActivityLog logUserLogout(User user, String sessionId) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .action(ActivityLog.Action.LOGOUT)
                .entityType(ActivityLog.EntityType.USER)
                .entityId(user.getId())
                .description(String.format("%s logged out", user.getName()))
                .sessionId(sessionId)
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log user registration
     */
    @Override
    public ActivityLog logUserRegistration(User user) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .action(ActivityLog.Action.REGISTER)
                .entityType(ActivityLog.EntityType.USER)
                .entityId(user.getId())
                .description(String.format("%s registered", user.getName()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    // ========== GROUP ACTIVITY LOGGING ==========

    /**
     * Log group creation
     */
    @Override
    public ActivityLog logGroupCreated(User user, Group group) {
        ActivityLog activityLog = ActivityLog.logGroupCreated(user, group);
        return activityLogRepository.save(activityLog);
    }

    /**
     * Log group update
     */
    @Override
    public ActivityLog logGroupUpdated(User user, Group group) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.UPDATE)
                .entityType(ActivityLog.EntityType.GROUP)
                .entityId(group.getId())
                .description(String.format("%s updated group '%s'", user.getName(), group.getName()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log member addition to group
     */
    @Override
    public ActivityLog logMemberAdded(User user, Group group, User newMember) {
        ActivityLog activityLog = ActivityLog.logMemberAdded(user, group, newMember);
        return activityLogRepository.save(activityLog);
    }

    /**
     * Log member removal from group
     */
    @Override
    public ActivityLog logMemberRemoved(User user, Group group, User removedMember) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.REMOVE_MEMBER)
                .entityType(ActivityLog.EntityType.GROUP)
                .entityId(group.getId())
                .description(String.format("%s removed {targetUser} from the group", user.getName()))
                .targetUser(removedMember.getName())
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log user joining group
     */
    @Override
    public ActivityLog logUserJoinedGroup(User user, Group group) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.JOIN_GROUP)
                .entityType(ActivityLog.EntityType.GROUP)
                .entityId(group.getId())
                .description(String.format("%s joined the group", user.getName()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log user leaving group
     */
    @Override
    public ActivityLog logUserLeftGroup(User user, Group group) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.LEAVE_GROUP)
                .entityType(ActivityLog.EntityType.GROUP)
                .entityId(group.getId())
                .description(String.format("%s left the group", user.getName()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log admin promotion
     */
    @Override
    public ActivityLog logAdminPromoted(User user, Group group, User promotedUser) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.PROMOTE_ADMIN)
                .entityType(ActivityLog.EntityType.GROUP)
                .entityId(group.getId())
                .description(String.format("%s promoted {targetUser} to admin", user.getName()))
                .targetUser(promotedUser.getName())
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log admin demotion
     */
    @Override
    public ActivityLog logAdminDemoted(User user, Group group, User demotedUser) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.DEMOTE_ADMIN)
                .entityType(ActivityLog.EntityType.GROUP)
                .entityId(group.getId())
                .description(String.format("%s demoted {targetUser} from admin", user.getName()))
                .targetUser(demotedUser.getName())
                .build();

        return activityLogRepository.save(activityLog);
    }

    // ========== EXPENSE ACTIVITY LOGGING ==========

    /**
     * Log expense creation
     */
    @Override
    public ActivityLog logExpenseCreated(User user, Group group, Expense expense) {
        ActivityLog activityLog = ActivityLog.logExpenseCreated(user, group, expense);
        return activityLogRepository.save(activityLog);
    }

    /**
     * Log expense update
     */
    @Override
    public ActivityLog logExpenseUpdated(User user, Group group, Expense expense) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.UPDATE)
                .entityType(ActivityLog.EntityType.EXPENSE)
                .entityId(expense.getId())
                .description(String.format("%s updated expense '%s'", user.getName(), expense.getDescription()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log expense deletion
     */
    @Override
    public ActivityLog logExpenseDeleted(User user, Group group, Expense expense) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.DELETE)
                .entityType(ActivityLog.EntityType.EXPENSE)
                .entityId(expense.getId())
                .description(String.format("%s deleted expense '%s'", user.getName(), expense.getDescription()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    // ========== SETTLEMENT ACTIVITY LOGGING ==========

    /**
     * Log settlement request
     */
    @Override
    public ActivityLog logSettlementRequested(User user, Group group, SettleUp settlement) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.REQUEST_SETTLEMENT)
                .entityType(ActivityLog.EntityType.SETTLEMENT)
                .entityId(settlement.getId())
                .description(String.format("%s requested settlement of %s",
                        user.getName(), settlement.getFormattedAmount()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log settlement completion
     */
    @Override
    public ActivityLog logSettlementCompleted(User user, Group group, SettleUp settlement) {
        ActivityLog activityLog = ActivityLog.logSettlementCompleted(user, group, settlement);
        return activityLogRepository.save(activityLog);
    }

    /**
     * Log settlement rejection
     */
    @Override
    public ActivityLog logSettlementRejected(User user, Group group, SettleUp settlement) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.REJECT_SETTLEMENT)
                .entityType(ActivityLog.EntityType.SETTLEMENT)
                .entityId(settlement.getId())
                .description(String.format("%s rejected settlement of %s",
                        user.getName(), settlement.getFormattedAmount()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log settlement updated
     */
    @Override
    public ActivityLog logSettlementUpdated(User user, Group group, SettleUp settlement) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.UPDATE)
                .entityType(ActivityLog.EntityType.SETTLEMENT)
                .entityId(settlement.getId())
                .description(String.format("%s updated settlement of %s",
                        user.getName(), settlement.getFormattedAmount()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log settlement deleted
     */
    @Override
    public ActivityLog logSettlementDeleted(User user, Group group, SettleUp settlement) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.DELETE)
                .entityType(ActivityLog.EntityType.SETTLEMENT)
                .entityId(settlement.getId())
                .description(String.format("%s deleted settlement of %s",
                        user.getName(), settlement.getFormattedAmount()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log settlement in progress
     */
    @Override
    public ActivityLog logSettlementInProgress(User user, Group group, SettleUp settlement) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.UPDATE)
                .entityType(ActivityLog.EntityType.SETTLEMENT)
                .entityId(settlement.getId())
                .description(String.format("%s marked settlement as in progress", user.getName()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log settlement cancelled
     */
    @Override
    public ActivityLog logSettlementCancelled(User user, Group group, SettleUp settlement) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.UPDATE)
                .entityType(ActivityLog.EntityType.SETTLEMENT)
                .entityId(settlement.getId())
                .description(String.format("%s cancelled settlement of %s",
                        user.getName(), settlement.getFormattedAmount()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    // ========== INVITATION ACTIVITY LOGGING ==========

    /**
     * Log invitation sent
     */
    @Override
    public ActivityLog logInvitationSent(User user, Group group, Invitation invitation) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.SEND_INVITATION)
                .entityType(ActivityLog.EntityType.INVITATION)
                .entityId(invitation.getId())
                .description(String.format("%s sent invitation to %s",
                        user.getName(), invitation.getInviteeDisplayName()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log invitation accepted
     */
    @Override
    public ActivityLog logInvitationAccepted(User user, Group group, Invitation invitation) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(group)
                .action(ActivityLog.Action.ACCEPT_INVITATION)
                .entityType(ActivityLog.EntityType.INVITATION)
                .entityId(invitation.getId())
                .description(String.format("%s accepted invitation to join the group", user.getName()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log invitation declined
     */
    @Override
    public ActivityLog logInvitationDeclined(String email, Group group, Invitation invitation) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(invitation.getInvitedBy()) // Use the inviter as the user for logging
                .group(group)
                .action(ActivityLog.Action.DECLINE_INVITATION)
                .entityType(ActivityLog.EntityType.INVITATION)
                .entityId(invitation.getId())
                .description(String.format("Invitation to %s was declined", email))
                .build();

        return activityLogRepository.save(activityLog);
    }

    // ========== QUERY OPERATIONS ==========

    /**
     * Get activity logs for a user
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLog> getActivityLogsForUser(User user, Pageable pageable) {
        return activityLogRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * Get activity logs for a group
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLog> getActivityLogsForGroup(Group group, Pageable pageable) {
        return activityLogRepository.findByGroupOrderByCreatedAtDesc(group, pageable);
    }

    /**
     * Get group activity feed
     */
    @Override
    @Transactional(readOnly = true)
    public List<ActivityLog> getGroupActivityFeed(Group group, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return activityLogRepository.findGroupActivityFeed(group, since);
    }

    /**
     * Get recent activities for a user
     */
    @Override
    @Transactional(readOnly = true)
    public List<ActivityLog> getRecentActivitiesForUser(User user, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return activityLogRepository.findRecentByUser(user, since);
    }

    /**
     * Get entity timeline
     */
    @Override
    @Transactional(readOnly = true)
    public List<ActivityLog> getEntityTimeline(ActivityLog.EntityType entityType, Long entityId) {
        return activityLogRepository.findEntityTimeline(entityType, entityId);
    }

    /**
     * Get user login history
     */
    @Override
    @Transactional(readOnly = true)
    public List<ActivityLog> getUserLoginHistory(User user) {
        return activityLogRepository.findLoginActivitiesByUser(user);
    }

    /**
     * Get last login for user
     */
    @Override
    @Transactional(readOnly = true)
    public ActivityLog getLastLoginForUser(User user) {
        return activityLogRepository.findLastLoginByUser(user);
    }

    // ========== ANALYTICS AND STATISTICS ==========

    /**
     * Get activity statistics by action
     */
    @Override
    @Transactional(readOnly = true)
    public Map<ActivityLog.Action, Long> getActivityStatsByAction() {
        List<Object[]> stats = activityLogRepository.countActivitiesByAction();
        return stats.stream()
                .collect(java.util.stream.Collectors.toMap(
                        stat -> (ActivityLog.Action) stat[0],
                        stat -> (Long) stat[1]));
    }

    /**
     * Get activity statistics by entity type
     */
    @Override
    @Transactional(readOnly = true)
    public Map<ActivityLog.EntityType, Long> getActivityStatsByEntityType() {
        List<Object[]> stats = activityLogRepository.countActivitiesByEntityType();
        return stats.stream()
                .collect(java.util.stream.Collectors.toMap(
                        stat -> (ActivityLog.EntityType) stat[0],
                        stat -> (Long) stat[1]));
    }

    /**
     * Get daily activity count for the last month
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getDailyActivityCount() {
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        List<Object[]> stats = activityLogRepository.getDailyActivityCount(monthAgo);
        return stats.stream()
                .collect(java.util.stream.Collectors.toMap(
                        stat -> stat[0].toString(),
                        stat -> (Long) stat[1]));
    }

    /**
     * Get user activity by group
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getUserActivityByGroup(User user) {
        List<Object[]> stats = activityLogRepository.findUserActivityByGroup(user);
        return stats.stream()
                .collect(java.util.stream.Collectors.toMap(
                        stat -> ((Group) stat[0]).getName(),
                        stat -> (Long) stat[1]));
    }

    // ========== MAINTENANCE OPERATIONS ==========

    /**
     * Clean up old activity logs
     */
    @Override
    @Transactional
    public int cleanupOldActivityLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        return activityLogRepository.deleteOldActivities(cutoffDate);
    }

    /**
     * Get count of old activities for cleanup estimation
     */
    @Override
    @Transactional(readOnly = true)
    public long getOldActivitiesCount(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        return activityLogRepository.countOldActivities(cutoffDate);
    }

    // ========== SECURITY MONITORING ==========

    /**
     * Get recent login attempts by IP
     */
    @Override
    @Transactional(readOnly = true)
    public List<ActivityLog> getRecentLoginsByIp(String ipAddress, int hours) {
        LocalDateTime recentThreshold = LocalDateTime.now().minusHours(hours);
        return activityLogRepository.findRecentLoginsByIp(ipAddress, recentThreshold);
    }

    /**
     * Get admin activities
     */
    @Override
    @Transactional(readOnly = true)
    public List<ActivityLog> getAdminActivities() {
        return activityLogRepository.findAdminActivities();
    }

    /**
     * Get suspicious activities (multiple login attempts, etc.)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ActivityLog> getSuspiciousActivities(String ipAddress, int hours) {
        return getRecentLoginsByIp(ipAddress, hours);
    }

    // ========== ATTACHMENT OPERATIONS ==========

    /**
     * Log attachment upload
     */
    @Override
    public ActivityLog logAttachmentUploaded(User user, Group group, Attachment attachment) {
        String description = String.format("Uploaded attachment '%s' (%s)",
                attachment.getOriginalFilename(),
                attachment.getFormattedFileSize());

        return createActivityLog(user, ActivityLog.Action.UPLOAD_ATTACHMENT,
                ActivityLog.EntityType.ATTACHMENT, attachment.getId(),
                description, group);
    }

    /**
     * Log attachment deletion
     */
    @Override
    public ActivityLog logAttachmentDeleted(User user, Group group, Attachment attachment) {
        String description = String.format("Deleted attachment '%s'",
                attachment.getOriginalFilename());

        return createActivityLog(user, ActivityLog.Action.DELETE_ATTACHMENT,
                ActivityLog.EntityType.ATTACHMENT, attachment.getId(),
                description, group);
    }

    /**
     * Log attachment download
     */
    @Override
    public ActivityLog logAttachmentDownloaded(User user, Group group, Attachment attachment) {
        String description = String.format("Downloaded attachment '%s'",
                attachment.getOriginalFilename());

        return createActivityLog(user, ActivityLog.Action.DOWNLOAD_ATTACHMENT,
                ActivityLog.EntityType.ATTACHMENT, attachment.getId(),
                description, group);
    }
    // ========== ADDITIONAL METHODS FOR SERVICE IMPLEMENTATIONS ==========

    /**
     * Log settlement created (simpler signature)
     */
    @Override
    public ActivityLog logSettlementCreated(SettleUp settlement, User user) {
        return logSettlementRequested(user, settlement.getGroup(), settlement);
    }

    /**
     * Log settlement confirmed (simpler signature)
     */
    @Override
    public ActivityLog logSettlementConfirmed(SettleUp settlement, User user) {
        return logSettlementCompleted(user, settlement.getGroup(), settlement);
    }

    /**
     * Log settlement rejected with reason (simpler signature)
     */
    @Override
    public ActivityLog logSettlementRejected(SettleUp settlement, User user, String reason) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(settlement.getGroup())
                .action(ActivityLog.Action.REJECT_SETTLEMENT)
                .entityType(ActivityLog.EntityType.SETTLEMENT)
                .entityId(settlement.getId())
                .description(String.format("%s rejected settlement of $%.2f. Reason: %s", 
                    user.getName(), settlement.getAmount(), reason))
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log invitation sent (simpler signature)
     */
    @Override
    public ActivityLog logInvitationSent(Invitation invitation, User user) {
        return logInvitationSent(user, invitation.getGroup(), invitation);
    }

    /**
     * Log invitation accepted (simpler signature)
     */
    @Override
    public ActivityLog logInvitationAccepted(Invitation invitation, User user) {
        return logInvitationAccepted(user, invitation.getGroup(), invitation);
    }

    /**
     * Log invitation declined with reason (simpler signature)
     */
    @Override
    public ActivityLog logInvitationDeclined(Invitation invitation, User user, String reason) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(invitation.getGroup())
                .action(ActivityLog.Action.DECLINE_INVITATION)
                .entityType(ActivityLog.EntityType.INVITATION)
                .entityId(invitation.getId())
                .description(String.format("%s declined invitation to join group. Reason: %s", 
                    user.getName(), reason))
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log invitation cancelled
     */
    @Override
    public ActivityLog logInvitationCancelled(Invitation invitation, User user) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .group(invitation.getGroup())
                .action(ActivityLog.Action.CANCEL_INVITATION)
                .entityType(ActivityLog.EntityType.INVITATION)
                .entityId(invitation.getId())
                .description(String.format("%s cancelled invitation to %s", 
                    user.getName(), invitation.getEmail()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    /**
     * Log attachment uploaded (simpler signature)
     */
    @Override
    public ActivityLog logAttachmentUploaded(Attachment attachment, User user) {
        return logAttachmentUploaded(user, attachment.getExpense().getGroup(), attachment);
    }

    /**
     * Log attachment deleted (simpler signature)
     */
    @Override
    public ActivityLog logAttachmentDeleted(Attachment attachment, User user) {
        return logAttachmentDeleted(user, attachment.getExpense().getGroup(), attachment);
    }
}