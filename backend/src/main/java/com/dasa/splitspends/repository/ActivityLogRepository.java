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

import com.dasa.splitspends.entity.ActivityLog;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

        // ========== BASIC QUERIES ==========

        /**
         * Find activity logs by user
         */
        List<ActivityLog> findByUserOrderByCreatedAtDesc(User user);

        /**
         * Find activity logs by user with pagination
         */
        Page<ActivityLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

        /**
         * Find activity logs by group
         */
        List<ActivityLog> findByGroupOrderByCreatedAtDesc(Group group);

        /**
         * Find activity logs by group with pagination
         */
        Page<ActivityLog> findByGroupOrderByCreatedAtDesc(Group group, Pageable pageable);

        /**
         * Find activity logs by action
         */
        List<ActivityLog> findByActionOrderByCreatedAtDesc(ActivityLog.Action action);

        /**
         * Find activity logs by entity type
         */
        List<ActivityLog> findByEntityTypeOrderByCreatedAtDesc(ActivityLog.EntityType entityType);

        // ========== USER-SPECIFIC QUERIES ==========

        /**
         * Find recent activity logs for a user (last 30 days)
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.user = :user " +
                        "AND a.createdAt >= :thirtyDaysAgo ORDER BY a.createdAt DESC")
        List<ActivityLog> findRecentByUser(@Param("user") User user,
                        @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

        /**
         * Find activity logs for a user in a specific group
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.user = :user AND a.group = :group " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findByUserAndGroup(@Param("user") User user, @Param("group") Group group);

        /**
         * Find user login activities
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.user = :user AND a.action = 'LOGIN' " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findLoginActivitiesByUser(@Param("user") User user);

        /**
         * Find user's last login activity
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.user = :user AND a.action = 'LOGIN' " +
                        "ORDER BY a.createdAt DESC LIMIT 1")
        ActivityLog findLastLoginByUser(@Param("user") User user);

        // ========== GROUP-SPECIFIC QUERIES ==========

        /**
         * Find group activity feed (recent activities in the group)
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.group = :group " +
                        "AND a.createdAt >= :since ORDER BY a.createdAt DESC")
        List<ActivityLog> findGroupActivityFeed(@Param("group") Group group,
                        @Param("since") LocalDateTime since);

        /**
         * Find group member activities (join, leave, add, remove)
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.group = :group " +
                        "AND a.action IN ('JOIN_GROUP', 'LEAVE_GROUP', 'ADD_MEMBER', 'REMOVE_MEMBER') " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findGroupMemberActivities(@Param("group") Group group);

        /**
         * Find expense-related activities in a group
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.group = :group " +
                        "AND a.entityType = 'EXPENSE' " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findExpenseActivitiesInGroup(@Param("group") Group group);

        /**
         * Find settlement activities in a group
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.group = :group " +
                        "AND a.entityType = 'SETTLEMENT' " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findSettlementActivitiesInGroup(@Param("group") Group group);

        // ========== ACTION-SPECIFIC QUERIES ==========

        /**
         * Find create activities
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.action = 'CREATE' " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findCreateActivities();

        /**
         * Find delete activities
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.action = 'DELETE' " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findDeleteActivities();

        /**
         * Find settlement-related activities
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.action IN " +
                        "('REQUEST_SETTLEMENT', 'ACCEPT_SETTLEMENT', 'REJECT_SETTLEMENT', 'COMPLETE') " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findSettlementActivities();

        /**
         * Find invitation-related activities
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.action IN " +
                        "('SEND_INVITATION', 'ACCEPT_INVITATION', 'DECLINE_INVITATION') " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findInvitationActivities();

        // ========== ENTITY-SPECIFIC QUERIES ==========

        /**
         * Find activities for a specific entity
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.entityType = :entityType " +
                        "AND a.entityId = :entityId ORDER BY a.createdAt DESC")
        List<ActivityLog> findByEntity(@Param("entityType") ActivityLog.EntityType entityType,
                        @Param("entityId") Long entityId);

        /**
         * Find expense activities
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.entityType = 'EXPENSE' " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findExpenseActivities();

        /**
         * Find group activities
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.entityType = 'GROUP' " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findGroupActivities();

        /**
         * Find user activities (activities on user entities)
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.entityType = 'USER' " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findUserEntityActivities();

        // ========== DATE-BASED QUERIES ==========

        /**
         * Find activities within date range
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.createdAt BETWEEN :startDate AND :endDate " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Find activities from today
         */
        @Query(value = "SELECT * FROM activity_log " +
                        "WHERE DATE(created_at) = CURRENT_DATE ORDER BY created_at DESC", nativeQuery = true)
        List<ActivityLog> findTodaysActivities();

        /**
         * Find activities from last week
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.createdAt >= :weekAgo " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findLastWeekActivities(@Param("weekAgo") LocalDateTime weekAgo);

        /**
         * Find recent activities (last 24 hours)
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.createdAt >= :dayAgo " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findRecentActivities(@Param("dayAgo") LocalDateTime dayAgo);

        // ========== SECURITY AND AUDIT QUERIES ==========

        /**
         * Find activities by IP address
         */
        List<ActivityLog> findByIpAddressOrderByCreatedAtDesc(String ipAddress);

        /**
         * Find activities by session ID
         */
        List<ActivityLog> findBySessionIdOrderByCreatedAtDesc(String sessionId);

        /**
         * Find suspicious activities (multiple failed logins, unusual patterns)
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.action = 'LOGIN' " +
                        "AND a.ipAddress = :ipAddress " +
                        "AND a.createdAt >= :recentThreshold " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findRecentLoginsByIp(@Param("ipAddress") String ipAddress,
                        @Param("recentThreshold") LocalDateTime recentThreshold);

        /**
         * Find admin activities
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.action IN " +
                        "('PROMOTE_ADMIN', 'DEMOTE_ADMIN', 'REMOVE_MEMBER') " +
                        "ORDER BY a.createdAt DESC")
        List<ActivityLog> findAdminActivities();

        // ========== STATISTICS QUERIES ==========

        /**
         * Count activities by action
         */
        @Query("SELECT a.action, COUNT(a) FROM ActivityLog a GROUP BY a.action")
        List<Object[]> countActivitiesByAction();

        /**
         * Count activities by entity type
         */
        @Query("SELECT a.entityType, COUNT(a) FROM ActivityLog a GROUP BY a.entityType")
        List<Object[]> countActivitiesByEntityType();

        /**
         * Count activities by user
         */
        @Query("SELECT a.user, COUNT(a) FROM ActivityLog a GROUP BY a.user ORDER BY COUNT(a) DESC")
        List<Object[]> countActivitiesByUser();

        /**
         * Count activities in date range
         */
        @Query("SELECT COUNT(a) FROM ActivityLog a WHERE a.createdAt BETWEEN :startDate AND :endDate")
        long countActivitiesInDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Get most active users (by activity count)
         */
        @Query("SELECT a.user FROM ActivityLog a GROUP BY a.user ORDER BY COUNT(a) DESC")
        Page<User> findMostActiveUsers(Pageable pageable);

        /**
         * Get daily activity count for the last month
         */
        @Query("SELECT DATE(a.createdAt), COUNT(a) FROM ActivityLog a " +
                        "WHERE a.createdAt >= :monthAgo " +
                        "GROUP BY DATE(a.createdAt) ORDER BY DATE(a.createdAt)")
        List<Object[]> getDailyActivityCount(@Param("monthAgo") LocalDateTime monthAgo);

        // ========== BULK OPERATIONS ==========

        /**
         * Delete old activity logs (older than specified date)
         */
        @Modifying
        @Query("DELETE FROM ActivityLog a WHERE a.createdAt < :cutoffDate")
        int deleteOldActivities(@Param("cutoffDate") LocalDateTime cutoffDate);

        /**
         * Count activities for cleanup (older than specified date)
         */
        @Query("SELECT COUNT(a) FROM ActivityLog a WHERE a.createdAt < :cutoffDate")
        long countOldActivities(@Param("cutoffDate") LocalDateTime cutoffDate);

        // ========== COMPLEX BUSINESS QUERIES ==========

        /**
         * Find activity timeline for a specific entity
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.entityType = :entityType " +
                        "AND a.entityId = :entityId ORDER BY a.createdAt ASC")
        List<ActivityLog> findEntityTimeline(@Param("entityType") ActivityLog.EntityType entityType,
                        @Param("entityId") Long entityId);

        /**
         * Find user interaction history (activities involving a target user)
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.user = :user " +
                        "AND a.targetUser = :targetUser ORDER BY a.createdAt DESC")
        List<ActivityLog> findUserInteractionHistory(@Param("user") User user,
                        @Param("targetUser") String targetUser);

        /**
         * Find cross-group activities by user
         */
        @Query("SELECT a.group, COUNT(a) FROM ActivityLog a WHERE a.user = :user " +
                        "AND a.group IS NOT NULL GROUP BY a.group ORDER BY COUNT(a) DESC")
        List<Object[]> findUserActivityByGroup(@Param("user") User user);

        /**
         * Find collaborative activities (activities involving multiple users)
         */
        @Query("SELECT a FROM ActivityLog a WHERE a.group = :group " +
                        "AND a.targetUser IS NOT NULL " +
                        "AND a.createdAt >= :since ORDER BY a.createdAt DESC")
        List<ActivityLog> findCollaborativeActivities(@Param("group") Group group,
                        @Param("since") LocalDateTime since);
}
