package com.dasa.splitspends.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.Notification;
import com.dasa.splitspends.entity.SettleUp;
import com.dasa.splitspends.entity.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
        /**
         * Find high priority notifications for a user by user ID
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId AND (n.priority = 'HIGH' OR n.priority = 'URGENT') ORDER BY n.createdAt DESC")
        List<Notification> findHighPriorityByUserId(@Param("userId") Long userId);

        /**
         * Find notifications for activity feed (recent, relevant notifications) by user
         * ID
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId AND n.createdAt >= :recentThreshold AND (n.expiresAt IS NULL OR n.expiresAt > CURRENT_TIMESTAMP) ORDER BY n.createdAt DESC")
        List<Notification> findActivityFeedNotifications(@Param("userId") Long userId,
                        @Param("recentThreshold") java.time.LocalDateTime recentThreshold);

        // ========== BASIC QUERIES ==========

        /**
         * Find notifications by recipient
         */
        List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

        /**
         * Find notifications by recipient user ID
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId ORDER BY n.createdAt DESC")
        List<Notification> findByRecipientIdOrderByCreatedAtDesc(@Param("userId") Long userId);

        /**
         * Find notifications by recipient with pagination
         */
        Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

        /**
         * Find notifications by recipient user ID with pagination
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId ORDER BY n.createdAt DESC")
        Page<Notification> findByRecipientIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

        /**
         * Find notifications by type
         */
        List<Notification> findByTypeOrderByCreatedAtDesc(Notification.NotificationType type);

        /**
         * Find notifications by priority
         */
        List<Notification> findByPriorityOrderByCreatedAtDesc(Notification.Priority priority);

        // ========== READ/UNREAD QUERIES ==========

        /**
         * Find unread notifications for a user
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient = :user AND n.isRead = false " +
                        "ORDER BY n.createdAt DESC")
        List<Notification> findUnreadByUser(@Param("user") User user);

        /**
         * Find unread notifications for a user by user ID
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
        List<Notification> findUnreadByUserId(@Param("userId") Long userId);

        /**
         * Find unread notifications for a user with pagination
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient = :user AND n.isRead = false " +
                        "ORDER BY n.createdAt DESC")
        Page<Notification> findUnreadByUser(@Param("user") User user, Pageable pageable);

        /**
         * Find read notifications for a user
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient = :user AND n.isRead = true " +
                        "ORDER BY n.readAt DESC")
        List<Notification> findReadByUser(@Param("user") User user);

        /**
         * Count unread notifications for a user
         */
        @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :user AND n.isRead = false")
        long countUnreadByUser(@Param("user") User user);

        /**
         * Count unread notifications for a user by user ID
         */
        @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :userId AND n.isRead = false")
        long countUnreadByUserId(@Param("userId") Long userId);

        /**
         * Count unread high priority notifications for a user
         */
        @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :user AND n.isRead = false " +
                        "AND (n.priority = 'HIGH' OR n.priority = 'URGENT')")
        long countUnreadHighPriorityByUser(@Param("user") User user);

        // ========== CONTEXT-SPECIFIC QUERIES ==========

        /**
         * Find notifications by group
         */
        List<Notification> findByGroupOrderByCreatedAtDesc(Group group);

        /**
         * Find notifications by expense
         */
        List<Notification> findByExpenseOrderByCreatedAtDesc(Expense expense);

        /**
         * Find notifications by settlement
         */
        List<Notification> findBySettleUpOrderByCreatedAtDesc(SettleUp settleUp);

        /**
         * Find notifications triggered by a specific user
         */
        List<Notification> findByTriggeredByOrderByCreatedAtDesc(User triggeredBy);

        /**
         * Find notifications for a user in a specific group
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient = :user AND n.group = :group " +
                        "ORDER BY n.createdAt DESC")
        List<Notification> findByUserAndGroup(@Param("user") User user, @Param("group") Group group);

        // ========== TYPE-SPECIFIC QUERIES ==========

        /**
         * Find expense notifications for a user
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient = :user " +
                        "AND n.type IN ('EXPENSE_ADDED', 'EXPENSE_UPDATED', 'EXPENSE_DELETED') " +
                        "ORDER BY n.createdAt DESC")
        List<Notification> findExpenseNotificationsByUser(@Param("user") User user);

        /**
         * Find settlement notifications for a user
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient = :user " +
                        "AND n.type IN ('SETTLEMENT_REQUEST', 'SETTLEMENT_COMPLETED', 'SETTLEMENT_REJECTED') " +
                        "ORDER BY n.createdAt DESC")
        List<Notification> findSettlementNotificationsByUser(@Param("user") User user);

        /**
         * Find group notifications for a user
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient = :user " +
                        "AND n.type IN ('GROUP_INVITATION', 'MEMBER_ADDED', 'MEMBER_REMOVED', 'GROUP_UPDATED') " +
                        "ORDER BY n.createdAt DESC")
        List<Notification> findGroupNotificationsByUser(@Param("user") User user);

        // ========== DATE-BASED QUERIES ==========

        /**
         * Find notifications within date range
         */
        @Query("SELECT n FROM Notification n WHERE n.createdAt BETWEEN :startDate AND :endDate " +
                        "ORDER BY n.createdAt DESC")
        List<Notification> findByDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Find recent notifications for a user (last 7 days)
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient = :user " +
                        "AND n.createdAt >= :sevenDaysAgo ORDER BY n.createdAt DESC")
        List<Notification> findRecentByUser(@Param("user") User user,
                        @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

        /**
         * Find notifications older than specified date
         */
        @Query("SELECT n FROM Notification n WHERE n.createdAt < :cutoffDate ORDER BY n.createdAt ASC")
        List<Notification> findOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

        // ========== EXPIRATION QUERIES ==========

        /**
         * Find expired notifications
         */
        @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NOT NULL " +
                        "AND n.expiresAt < CURRENT_TIMESTAMP ORDER BY n.expiresAt ASC")
        List<Notification> findExpiredNotifications();

        /**
         * Find notifications expiring soon (within next 24 hours)
         */
        @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NOT NULL " +
                        "AND n.expiresAt BETWEEN CURRENT_TIMESTAMP AND :tomorrow " +
                        "ORDER BY n.expiresAt ASC")
        List<Notification> findExpiringSoon(@Param("tomorrow") LocalDateTime tomorrow);

        /**
         * Find non-expired notifications for a user
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient = :user " +
                        "AND (n.expiresAt IS NULL OR n.expiresAt > CURRENT_TIMESTAMP) " +
                        "ORDER BY n.createdAt DESC")
        List<Notification> findNonExpiredByUser(@Param("user") User user);

        // ========== BULK OPERATIONS ==========

        /**
         * Mark all notifications as read for a user
         */
        @Modifying
        @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP " +
                        "WHERE n.recipient = :user AND n.isRead = false")
        int markAllAsReadForUser(@Param("user") User user);

        /**
         * Mark all notifications as read for a user by user ID
         */
        @Modifying
        @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipient.id = :userId AND n.isRead = false")
        int markAllAsReadForUser(@Param("userId") Long userId);

        /**
         * Mark notifications of specific type as read for a user
         */
        @Modifying
        @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP " +
                        "WHERE n.recipient = :user AND n.type = :type AND n.isRead = false")
        int markAsReadByTypeForUser(@Param("user") User user, @Param("type") Notification.NotificationType type);

        /**
         * Delete old notifications (read or unread) older than specified date
         */
        @Modifying
        @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
        int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

        /**
         * Delete old read notifications (older than specified date)
         */
        @Modifying
        @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.readAt < :cutoffDate")
        int deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

        // ========== STATISTICS QUERIES ==========

        /**
         * Count notifications by type for a user
         */
        @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :user AND n.type = :type")
        long countByUserAndType(@Param("user") User user, @Param("type") Notification.NotificationType type);

        /**
         * Count notifications by priority for a user
         */
        @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :user AND n.priority = :priority")
        long countByUserAndPriority(@Param("user") User user, @Param("priority") Notification.Priority priority);

        /**
         * Get notification statistics for a user
         */
        @Query("SELECT n.type, COUNT(n) FROM Notification n WHERE n.recipient = :user GROUP BY n.type")
        List<Object[]> getNotificationStatsByUser(@Param("user") User user);

        // ========== COMPLEX BUSINESS QUERIES ==========

        /**
         * Find duplicate notifications (same type, user, and related entity)
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient = :user AND n.type = :type " +
                        "AND n.expense = :expense AND n.createdAt > :since ORDER BY n.createdAt DESC")
        List<Notification> findDuplicateExpenseNotifications(@Param("user") User user,
                        @Param("type") Notification.NotificationType type,
                        @Param("expense") Expense expense,
                        @Param("since") LocalDateTime since);

        /**
         * Find pending action notifications (high priority unread notifications)
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient = :user AND n.isRead = false " +
                        "AND (n.priority = 'HIGH' OR n.priority = 'URGENT') " +
                        "AND (n.expiresAt IS NULL OR n.expiresAt > CURRENT_TIMESTAMP) " +
                        "ORDER BY n.priority DESC, n.createdAt DESC")
        List<Notification> findPendingActionNotifications(@Param("user") User user);

        /**
         * Find notifications for activity feed (recent, relevant notifications)
         */
        @Query("SELECT n FROM Notification n WHERE n.recipient = :user " +
                        "AND n.createdAt >= :recentThreshold " +
                        "AND (n.expiresAt IS NULL OR n.expiresAt > CURRENT_TIMESTAMP) " +
                        "ORDER BY n.createdAt DESC")
        List<Notification> findActivityFeedNotifications(@Param("user") User user,
                        @Param("recentThreshold") LocalDateTime recentThreshold);

}
