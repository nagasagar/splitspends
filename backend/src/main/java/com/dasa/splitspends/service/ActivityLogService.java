package com.dasa.splitspends.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.dasa.splitspends.entity.ActivityLog;
import com.dasa.splitspends.entity.Attachment;
import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.Invitation;
import com.dasa.splitspends.entity.SettleUp;
import com.dasa.splitspends.entity.User;

public interface ActivityLogService {
    ActivityLog createActivityLog(User user, ActivityLog.Action action, ActivityLog.EntityType entityType, Long entityId, String description, Group group);
    ActivityLog getActivityLogById(Long id);

    // User activity
    ActivityLog logUserLogin(User user, String ipAddress, String userAgent, String sessionId);
    ActivityLog logUserLogout(User user, String sessionId);
    ActivityLog logUserRegistration(User user);

    // Group activity
    ActivityLog logGroupCreated(User user, Group group);
    ActivityLog logGroupUpdated(User user, Group group);
    ActivityLog logMemberAdded(User user, Group group, User newMember);
            
            
    ActivityLog logMemberRemoved(User user, Group group, User removedMember);
    ActivityLog logUserJoinedGroup(User user, Group group);
    ActivityLog logUserLeftGroup(User user, Group group);
    ActivityLog logAdminPromoted(User user, Group group, User promotedUser);
    ActivityLog logAdminDemoted(User user, Group group, User demotedUser);

    // Expense activity
    ActivityLog logExpenseCreated(User user, Group group, Expense expense);
    ActivityLog logExpenseUpdated(User user, Group group, Expense expense);
    ActivityLog logExpenseDeleted(User user, Group group, Expense expense);

    // Settlement activity
    ActivityLog logSettlementRequested(User user, Group group, SettleUp settlement);
    ActivityLog logSettlementCompleted(User user, Group group, SettleUp settlement);
    ActivityLog logSettlementRejected(User user, Group group, SettleUp settlement);
    ActivityLog logSettlementUpdated(User user, Group group, SettleUp settlement);
    ActivityLog logSettlementDeleted(User user, Group group, SettleUp settlement);
    ActivityLog logSettlementInProgress(User user, Group group, SettleUp settlement);
    ActivityLog logSettlementCancelled(User user, Group group, SettleUp settlement);
    
    // Additional settlement methods
    ActivityLog logSettlementCreated(SettleUp settlement, User user);
    ActivityLog logSettlementConfirmed(SettleUp settlement, User user);
    ActivityLog logSettlementRejected(SettleUp settlement, User user, String reason);

    // Invitation activity
    ActivityLog logInvitationSent(User user, Group group, Invitation invitation);
    ActivityLog logInvitationAccepted(User user, Group group, Invitation invitation);
    ActivityLog logInvitationDeclined(String email, Group group, Invitation invitation);
    
    // Additional invitation methods
    ActivityLog logInvitationSent(Invitation invitation, User user);
    ActivityLog logInvitationAccepted(Invitation invitation, User user);
    ActivityLog logInvitationDeclined(Invitation invitation, User user, String reason);
    ActivityLog logInvitationCancelled(Invitation invitation, User user);

    // Attachment activity
    ActivityLog logAttachmentUploaded(User user, Group group, Attachment attachment);
    ActivityLog logAttachmentDeleted(User user, Group group, Attachment attachment);
    ActivityLog logAttachmentDownloaded(User user, Group group, Attachment attachment);
    
    // Additional attachment methods
    ActivityLog logAttachmentUploaded(Attachment attachment, User user);
    ActivityLog logAttachmentDeleted(Attachment attachment, User user);

    // Query operations
    Page<ActivityLog> getActivityLogsForUser(User user, Pageable pageable);
    Page<ActivityLog> getActivityLogsForGroup(Group group, Pageable pageable);
    List<ActivityLog> getGroupActivityFeed(Group group, int days);
    List<ActivityLog> getRecentActivitiesForUser(User user, int days);
    List<ActivityLog> getEntityTimeline(ActivityLog.EntityType entityType, Long entityId);
    List<ActivityLog> getUserLoginHistory(User user);
    ActivityLog getLastLoginForUser(User user);

    // Analytics/statistics
    Map<ActivityLog.Action, Long> getActivityStatsByAction();
    Map<ActivityLog.EntityType, Long> getActivityStatsByEntityType();
    Map<String, Long> getDailyActivityCount();
    Map<String, Long> getUserActivityByGroup(User user);

    // Maintenance
    int cleanupOldActivityLogs(int daysToKeep);
    long getOldActivitiesCount(int daysToKeep);

    // Security
    List<ActivityLog> getRecentLoginsByIp(String ipAddress, int hours);
    List<ActivityLog> getAdminActivities();
    List<ActivityLog> getSuspiciousActivities(String ipAddress, int hours);
}