package com.dasa.splitspends.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.Invitation;
import com.dasa.splitspends.entity.Notification;
import com.dasa.splitspends.entity.SettleUp;
import com.dasa.splitspends.entity.User;

public interface NotificationService {

    /**
     * Create a general notification.
     */
    Notification createNotification(User recipient, String title, String message, 
                                  Notification.NotificationType type, Notification.Priority priority);

    /**
     * Get user notifications.
     */
    List<Notification> getUserNotifications(Long userId, boolean unreadOnly);

    /**
     * Get user notifications with pagination.
     */
    Page<Notification> getUserNotificationsPaginated(Long userId, Pageable pageable);

    /**
     * Mark notification as read.
     */
    Notification markAsRead(Long notificationId);

    /**
     * Mark all notifications as read for a user.
     */
    int markAllAsRead(Long userId);

    /**
     * Get unread notification count.
     */
    long getUnreadCount(Long userId);

    /**
     * Delete a notification.
     */
    void deleteNotification(Long notificationId);

    /**
     * Delete old notifications.
     */
    void deleteOldNotifications(int daysOld);

    /**
     * Send expense-related notifications.
     */
    void sendExpenseNotification(Expense expense);
    void sendExpenseUpdatedNotification(Expense expense, User updatedBy);

    /**
     * Send settlement-related notifications.
     */
    void sendSettlementNotification(SettleUp settlement);
    void sendSettlementConfirmedNotification(SettleUp settlement);
    void sendSettlementRejectedNotification(SettleUp settlement);
    void sendSettlementReminderNotification(SettleUp settlement);

    /**
     * Send group-related notifications.
     */
    void sendGroupInvitationNotification(Invitation invitation);
    void sendGroupMemberAddedNotification(Group group, User newMember, User addedBy);

    /**
     * Get activity feed notifications.
     */
    List<Notification> getActivityFeedNotifications(Long userId, LocalDateTime since);

    /**
     * Get high priority notifications.
     */
    List<Notification> getHighPriorityNotifications(Long userId);
}