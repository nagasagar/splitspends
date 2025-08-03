package com.dasa.splitspends.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.Notification;
import com.dasa.splitspends.entity.SettleUp;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.NotificationRepository;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // ========== BASIC CRUD OPERATIONS ==========

    /**
     * Create a new notification
     */
    public Notification createNotification(User recipient, Notification.NotificationType type,
            String title, String message, User triggeredBy) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .triggeredBy(triggeredBy)
                .priority(Notification.Priority.NORMAL)
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * Get notification by ID
     */
    @Transactional(readOnly = true)
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
    }

    /**
     * Mark notification as read
     */
    public Notification markAsRead(Long id, User user) {
        Notification notification = getNotificationById(id);

        validateNotificationAccess(notification, user);

        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    /**
     * Mark notification as unread
     */
    public Notification markAsUnread(Long id, User user) {
        Notification notification = getNotificationById(id);

        validateNotificationAccess(notification, user);

        notification.markAsUnread();
        return notificationRepository.save(notification);
    }

    /**
     * Delete notification
     */
    public void deleteNotification(Long id, User user) {
        Notification notification = getNotificationById(id);

        validateNotificationAccess(notification, user);

        notificationRepository.delete(notification);
    }

    // ========== BULK OPERATIONS ==========

    /**
     * Mark all notifications as read for a user
     */
    public int markAllAsReadForUser(User user) {
        return notificationRepository.markAllAsReadForUser(user);
    }

    /**
     * Mark notifications of specific type as read for a user
     */
    public int markAsReadByType(User user, Notification.NotificationType type) {
        return notificationRepository.markAsReadByTypeForUser(user, type);
    }

    // ========== QUERY OPERATIONS ==========

    /**
     * Get notifications for a user with pagination
     */
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsForUser(User user, Pageable pageable) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findUnreadByUser(user);
    }

    /**
     * Get unread notifications count for a user
     */
    @Transactional(readOnly = true)
    public long getUnreadNotificationsCount(User user) {
        return notificationRepository.countUnreadByUser(user);
    }

    /**
     * Get high priority unread notifications count
     */
    @Transactional(readOnly = true)
    public long getUnreadHighPriorityCount(User user) {
        return notificationRepository.countUnreadHighPriorityByUser(user);
    }

    /**
     * Get recent notifications for a user
     */
    @Transactional(readOnly = true)
    public List<Notification> getRecentNotifications(User user) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return notificationRepository.findRecentByUser(user, sevenDaysAgo);
    }

    /**
     * Get notifications for a group
     */
    @Transactional(readOnly = true)
    public List<Notification> getGroupNotifications(Group group) {
        return notificationRepository.findByGroupOrderByCreatedAtDesc(group);
    }

    /**
     * Get pending action notifications for a user
     */
    @Transactional(readOnly = true)
    public List<Notification> getPendingActionNotifications(User user) {
        return notificationRepository.findPendingActionNotifications(user);
    }

    /**
     * Get activity feed notifications for a user
     */
    @Transactional(readOnly = true)
    public List<Notification> getActivityFeedNotifications(User user) {
        LocalDateTime recentThreshold = LocalDateTime.now().minusDays(30);
        return notificationRepository.findActivityFeedNotifications(user, recentThreshold);
    }

    // ========== FACTORY METHODS FOR SPECIFIC NOTIFICATION TYPES ==========

    /**
     * Create expense added notification
     */
    public void createExpenseAddedNotification(User recipient, User triggeredBy,
            Expense expense, Group group) {
        if (shouldCreateNotification(recipient, triggeredBy)) {
            Notification notification = Notification.createExpenseAddedNotification(
                    recipient, triggeredBy, expense, group);
            notificationRepository.save(notification);
        }
    }

    /**
     * Create expense updated notification
     */
    public void createExpenseUpdatedNotification(User recipient, User triggeredBy,
            Expense expense, Group group) {
        if (shouldCreateNotification(recipient, triggeredBy)) {
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .triggeredBy(triggeredBy)
                    .expense(expense)
                    .group(group)
                    .type(Notification.NotificationType.EXPENSE_UPDATED)
                    .title("Expense Updated")
                    .message(String.format("%s updated expense '%s'",
                            triggeredBy.getName(), expense.getDescription()))
                    .priority(Notification.Priority.NORMAL)
                    .build();

            notificationRepository.save(notification);
        }
    }

    /**
     * Create settlement request notification
     */
    public void createSettlementRequestNotification(User recipient, User triggeredBy,
            SettleUp settleUp, Group group) {
        if (shouldCreateNotification(recipient, triggeredBy)) {
            Notification notification = Notification.createSettlementRequestNotification(
                    recipient, triggeredBy, settleUp, group);
            notificationRepository.save(notification);
        }
    }

    /**
     * Create settlement completed notification
     */
    public void createSettlementCompletedNotification(User recipient, User triggeredBy,
            SettleUp settleUp, Group group) {
        if (shouldCreateNotification(recipient, triggeredBy)) {
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .triggeredBy(triggeredBy)
                    .settleUp(settleUp)
                    .group(group)
                    .type(Notification.NotificationType.SETTLEMENT_COMPLETED)
                    .title("Settlement Completed")
                    .message(String.format("Settlement of %s has been completed",
                            settleUp.getFormattedAmount()))
                    .priority(Notification.Priority.HIGH)
                    .build();

            notificationRepository.save(notification);
        }
    }

    /**
     * Create settlement rejected notification
     */
    public void createSettlementRejectedNotification(User recipient, User triggeredBy,
            SettleUp settleUp, Group group) {
        if (shouldCreateNotification(recipient, triggeredBy)) {
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .triggeredBy(triggeredBy)
                    .settleUp(settleUp)
                    .group(group)
                    .type(Notification.NotificationType.SETTLEMENT_REJECTED)
                    .title("Settlement Rejected")
                    .message(String.format("%s rejected settlement of %s",
                            triggeredBy.getName(), settleUp.getFormattedAmount()))
                    .priority(Notification.Priority.HIGH)
                    .build();

            notificationRepository.save(notification);
        }
    }

    /**
     * Create settlement reminder notification
     */
    public void createSettlementReminderNotification(User recipient, User triggeredBy,
            SettleUp settleUp, Group group) {
        if (shouldCreateNotification(recipient, triggeredBy)) {
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .triggeredBy(triggeredBy)
                    .settleUp(settleUp)
                    .group(group)
                    .type(Notification.NotificationType.PAYMENT_DUE)
                    .title("Payment Reminder")
                    .message(String.format("Reminder: You have a pending settlement of %s",
                            settleUp.getFormattedAmount()))
                    .priority(Notification.Priority.HIGH)
                    .build();

            notificationRepository.save(notification);
        }
    }

    /**
     * Create group invitation notification
     */
    public void createGroupInvitationNotification(User recipient, User triggeredBy, Group group) {
        if (shouldCreateNotification(recipient, triggeredBy)) {
            Notification notification = Notification.createGroupInvitationNotification(
                    recipient, triggeredBy, group);
            notificationRepository.save(notification);
        }
    }

    /**
     * Create member added notification
     */
    public void createMemberAddedNotification(User recipient, User triggeredBy,
            User newMember, Group group) {
        if (shouldCreateNotification(recipient, triggeredBy)) {
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .triggeredBy(triggeredBy)
                    .group(group)
                    .type(Notification.NotificationType.MEMBER_ADDED)
                    .title("New Member Added")
                    .message(String.format("%s added %s to the group",
                            triggeredBy.getName(), newMember.getName()))
                    .priority(Notification.Priority.NORMAL)
                    .build();

            notificationRepository.save(notification);
        }
    }

    /**
     * Create member removed notification
     */
    public void createMemberRemovedNotification(User recipient, User triggeredBy,
            User removedMember, Group group) {
        if (shouldCreateNotification(recipient, triggeredBy)) {
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .triggeredBy(triggeredBy)
                    .group(group)
                    .type(Notification.NotificationType.MEMBER_REMOVED)
                    .title("Member Removed")
                    .message(String.format("%s removed %s from the group",
                            triggeredBy.getName(), removedMember.getName()))
                    .priority(Notification.Priority.NORMAL)
                    .build();

            notificationRepository.save(notification);
        }
    }

    /**
     * Create balance reminder notification
     */
    public void createBalanceReminderNotification(User recipient, Group group, String details) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .group(group)
                .type(Notification.NotificationType.BALANCE_REMINDER)
                .title("Balance Reminder")
                .message(details)
                .priority(Notification.Priority.NORMAL)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * Create system notification
     */
    public void createSystemNotification(User recipient, String title, String message,
            Notification.Priority priority) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(Notification.NotificationType.SYSTEM_UPDATE)
                .title(title)
                .message(message)
                .priority(priority)
                .build();

        notificationRepository.save(notification);
    }

    // ========== MAINTENANCE OPERATIONS ==========

    /**
     * Clean up expired notifications
     */
    @Transactional
    public void cleanupExpiredNotifications() {
        List<Notification> expiredNotifications = notificationRepository.findExpiredNotifications();
        notificationRepository.deleteAll(expiredNotifications);
    }

    /**
     * Clean up old read notifications
     */
    @Transactional
    public int cleanupOldReadNotifications(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        return notificationRepository.deleteOldReadNotifications(cutoffDate);
    }

    /**
     * Get notification statistics for a user
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getNotificationStats(User user) {
        List<Object[]> stats = notificationRepository.getNotificationStatsByUser(user);
        return stats.stream()
                .collect(java.util.stream.Collectors.toMap(
                        stat -> ((Notification.NotificationType) stat[0]).name(),
                        stat -> (Long) stat[1]));
    }

    // ========== VALIDATION METHODS ==========

    private void validateNotificationAccess(Notification notification, User user) {
        if (!notification.getRecipient().equals(user)) {
            throw new IllegalArgumentException("User can only access their own notifications");
        }
    }

    private boolean shouldCreateNotification(User recipient, User triggeredBy) {
        // Don't create notifications for actions user performed themselves
        if (recipient.equals(triggeredBy)) {
            return false;
        }

        // Check user notification preferences
        if (recipient.getEmailNotifications() != null && !recipient.getEmailNotifications()) {
            return false;
        }

        return true;
    }

    /**
     * Check for duplicate notifications to avoid spam
     */
    @SuppressWarnings("unused")
    private boolean isDuplicateNotification(User recipient, Notification.NotificationType type,
            Expense expense, LocalDateTime since) {
        List<Notification> duplicates = notificationRepository.findDuplicateExpenseNotifications(
                recipient, type, expense, since);
        return !duplicates.isEmpty();
    }
}
