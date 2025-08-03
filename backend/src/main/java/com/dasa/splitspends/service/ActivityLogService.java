package com.dasa.splitspends.service;

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

@Service
@Transactional
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    // ========== BASIC CRUD OPERATIONS ==========

    /**
     * Create a generic activity log
     */
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
    @Transactional(readOnly = true)
    public ActivityLog getActivityLogById(Long id) {
        return activityLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activity log not found"));
    }

    // ========== USER ACTIVITY LOGGING ==========

    /**
     * Log user login
     */
    public ActivityLog logUserLogin(User user, String ipAddress, String userAgent, String sessionId) {
        ActivityLog activityLog = ActivityLog.logUserLogin(user, ipAddress, userAgent);
        activityLog.setSessionId(sessionId);
        return activityLogRepository.save(activityLog);
    }

    /**
     * Log user logout
     */
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
    public ActivityLog logGroupCreated(User user, Group group) {
        ActivityLog activityLog = ActivityLog.logGroupCreated(user, group);
        return activityLogRepository.save(activityLog);
    }

    /**
     * Log group update
     */
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
    public ActivityLog logMemberAdded(User user, Group group, User newMember) {
        ActivityLog activityLog = ActivityLog.logMemberAdded(user, group, newMember);
        return activityLogRepository.save(activityLog);
    }

    /**
     * Log member removal from group
     */
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
    public ActivityLog logExpenseCreated(User user, Group group, Expense expense) {
        ActivityLog activityLog = ActivityLog.logExpenseCreated(user, group, expense);
        return activityLogRepository.save(activityLog);
    }

    /**
     * Log expense update
     */
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
    public ActivityLog logSettlementCompleted(User user, Group group, SettleUp settlement) {
        ActivityLog activityLog = ActivityLog.logSettlementCompleted(user, group, settlement);
        return activityLogRepository.save(activityLog);
    }

    /**
     * Log settlement rejection
     */
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
    @Transactional(readOnly = true)
    public Page<ActivityLog> getActivityLogsForUser(User user, Pageable pageable) {
        return activityLogRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * Get activity logs for a group
     */
    @Transactional(readOnly = true)
    public Page<ActivityLog> getActivityLogsForGroup(Group group, Pageable pageable) {
        return activityLogRepository.findByGroupOrderByCreatedAtDesc(group, pageable);
    }

    /**
     * Get group activity feed
     */
    @Transactional(readOnly = true)
    public List<ActivityLog> getGroupActivityFeed(Group group, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return activityLogRepository.findGroupActivityFeed(group, since);
    }

    /**
     * Get recent activities for a user
     */
    @Transactional(readOnly = true)
    public List<ActivityLog> getRecentActivitiesForUser(User user, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return activityLogRepository.findRecentByUser(user, since);
    }

    /**
     * Get entity timeline
     */
    @Transactional(readOnly = true)
    public List<ActivityLog> getEntityTimeline(ActivityLog.EntityType entityType, Long entityId) {
        return activityLogRepository.findEntityTimeline(entityType, entityId);
    }

    /**
     * Get user login history
     */
    @Transactional(readOnly = true)
    public List<ActivityLog> getUserLoginHistory(User user) {
        return activityLogRepository.findLoginActivitiesByUser(user);
    }

    /**
     * Get last login for user
     */
    @Transactional(readOnly = true)
    public ActivityLog getLastLoginForUser(User user) {
        return activityLogRepository.findLastLoginByUser(user);
    }

    // ========== ANALYTICS AND STATISTICS ==========

    /**
     * Get activity statistics by action
     */
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
    @Transactional
    public int cleanupOldActivityLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        return activityLogRepository.deleteOldActivities(cutoffDate);
    }

    /**
     * Get count of old activities for cleanup estimation
     */
    @Transactional(readOnly = true)
    public long getOldActivitiesCount(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        return activityLogRepository.countOldActivities(cutoffDate);
    }

    // ========== SECURITY MONITORING ==========

    /**
     * Get recent login attempts by IP
     */
    @Transactional(readOnly = true)
    public List<ActivityLog> getRecentLoginsByIp(String ipAddress, int hours) {
        LocalDateTime recentThreshold = LocalDateTime.now().minusHours(hours);
        return activityLogRepository.findRecentLoginsByIp(ipAddress, recentThreshold);
    }

    /**
     * Get admin activities
     */
    @Transactional(readOnly = true)
    public List<ActivityLog> getAdminActivities() {
        return activityLogRepository.findAdminActivities();
    }

    /**
     * Get suspicious activities (multiple login attempts, etc.)
     */
    @Transactional(readOnly = true)
    public List<ActivityLog> getSuspiciousActivities(String ipAddress, int hours) {
        return getRecentLoginsByIp(ipAddress, hours);
    }

    // ========== ATTACHMENT OPERATIONS ==========

    /**
     * Log attachment upload
     */
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
    public ActivityLog logAttachmentDownloaded(User user, Group group, Attachment attachment) {
        String description = String.format("Downloaded attachment '%s'",
                attachment.getOriginalFilename());

        return createActivityLog(user, ActivityLog.Action.DOWNLOAD_ATTACHMENT,
                ActivityLog.EntityType.ATTACHMENT, attachment.getId(),
                description, group);
    }
}
