package com.dasa.splitspends.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.Invitation;
import com.dasa.splitspends.entity.User;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    // ========== BASIC QUERIES ==========

    /**
     * Find invitation by token
     */
    Optional<Invitation> findByInvitationToken(String invitationToken);

    /**
     * Find invitations by group
     */
    List<Invitation> findByGroupOrderByCreatedAtDesc(Group group);

    /**
     * Find invitations by invited user (inviter)
     */
    List<Invitation> findByInvitedByOrderByCreatedAtDesc(User invitedBy);

    /**
     * Find invitations by email
     */
    List<Invitation> findByEmailOrderByCreatedAtDesc(String email);

    /**
     * Find invitations by status
     */
    List<Invitation> findByStatusOrderByCreatedAtDesc(Invitation.InvitationStatus status);

    // ========== STATUS-SPECIFIC QUERIES ==========

    /**
     * Find pending invitations
     */
    @Query("SELECT i FROM Invitation i WHERE i.status = 'PENDING' ORDER BY i.createdAt DESC")
    List<Invitation> findPendingInvitations();

    /**
     * Find pending invitations for a group
     */
    @Query("SELECT i FROM Invitation i WHERE i.group = :group AND i.status = 'PENDING' " +
            "ORDER BY i.createdAt DESC")
    List<Invitation> findPendingByGroup(@Param("group") Group group);

    /**
     * Find pending invitations by email
     */
    @Query("SELECT i FROM Invitation i WHERE i.email = :email AND i.status = 'PENDING' " +
            "ORDER BY i.createdAt DESC")
    List<Invitation> findPendingByEmail(@Param("email") String email);

    /**
     * Find accepted invitations by group
     */
    @Query("SELECT i FROM Invitation i WHERE i.group = :group AND i.status = 'ACCEPTED' " +
            "ORDER BY i.acceptedAt DESC")
    List<Invitation> findAcceptedByGroup(@Param("group") Group group);

    /**
     * Find declined invitations by group
     */
    @Query("SELECT i FROM Invitation i WHERE i.group = :group AND i.status = 'DECLINED' " +
            "ORDER BY i.declinedAt DESC")
    List<Invitation> findDeclinedByGroup(@Param("group") Group group);

    // ========== EXPIRATION QUERIES ==========

    /**
     * Find expired invitations
     */
    @Query("SELECT i FROM Invitation i WHERE i.expiresAt < CURRENT_TIMESTAMP " +
            "AND i.status = 'PENDING' ORDER BY i.expiresAt ASC")
    List<Invitation> findExpiredInvitations();

    /**
     * Find invitations expiring soon (within next 24 hours)
     */
    @Query("SELECT i FROM Invitation i WHERE i.status = 'PENDING' " +
            "AND i.expiresAt BETWEEN CURRENT_TIMESTAMP AND :tomorrow " +
            "ORDER BY i.expiresAt ASC")
    List<Invitation> findExpiringSoon(@Param("tomorrow") LocalDateTime tomorrow);

    /**
     * Find valid (non-expired, pending) invitations
     */
    @Query("SELECT i FROM Invitation i WHERE i.status = 'PENDING' " +
            "AND i.expiresAt > CURRENT_TIMESTAMP ORDER BY i.createdAt DESC")
    List<Invitation> findValidInvitations();

    /**
     * Find valid invitations for an email
     */
    @Query("SELECT i FROM Invitation i WHERE i.email = :email AND i.status = 'PENDING' " +
            "AND i.expiresAt > CURRENT_TIMESTAMP ORDER BY i.createdAt DESC")
    List<Invitation> findValidByEmail(@Param("email") String email);

    /**
     * Find valid invitations for a group
     */
    @Query("SELECT i FROM Invitation i WHERE i.group = :group AND i.status = 'PENDING' " +
            "AND i.expiresAt > CURRENT_TIMESTAMP ORDER BY i.createdAt DESC")
    List<Invitation> findValidByGroup(@Param("group") Group group);

    // ========== EMAIL AND REMINDER QUERIES ==========

    /**
     * Find invitations where email hasn't been sent
     */
    @Query("SELECT i FROM Invitation i WHERE i.emailSent = false AND i.status = 'PENDING' " +
            "ORDER BY i.createdAt ASC")
    List<Invitation> findUnsentInvitations();

    /**
     * Find invitations that need reminders
     */
    @Query("SELECT i FROM Invitation i WHERE i.status = 'PENDING' " +
            "AND i.expiresAt > CURRENT_TIMESTAMP " +
            "AND i.reminderCount < 3 " +
            "AND (i.lastReminderSentAt IS NULL OR i.lastReminderSentAt < :reminderThreshold) " +
            "ORDER BY i.createdAt ASC")
    List<Invitation> findInvitationsNeedingReminder(@Param("reminderThreshold") LocalDateTime reminderThreshold);

    /**
     * Find invitations sent but not yet reminded
     */
    @Query("SELECT i FROM Invitation i WHERE i.status = 'PENDING' " +
            "AND i.emailSent = true " +
            "AND i.reminderCount = 0 " +
            "AND i.createdAt < :reminderThreshold " +
            "ORDER BY i.createdAt ASC")
    List<Invitation> findInvitationsForFirstReminder(@Param("reminderThreshold") LocalDateTime reminderThreshold);

    // ========== DATE-BASED QUERIES ==========

    /**
     * Find invitations created within date range
     */
    @Query("SELECT i FROM Invitation i WHERE i.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY i.createdAt DESC")
    List<Invitation> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find invitations accepted within date range
     */
    @Query("SELECT i FROM Invitation i WHERE i.acceptedAt BETWEEN :startDate AND :endDate " +
            "ORDER BY i.acceptedAt DESC")
    List<Invitation> findAcceptedInDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find recent invitations for a group (last 30 days)
     */
    @Query("SELECT i FROM Invitation i WHERE i.group = :group " +
            "AND i.createdAt >= :thirtyDaysAgo ORDER BY i.createdAt DESC")
    List<Invitation> findRecentByGroup(@Param("group") Group group,
            @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

    // ========== USER-SPECIFIC QUERIES ==========

    /**
     * Find invitations sent by a user with pagination
     */
    Page<Invitation> findByInvitedByOrderByCreatedAtDesc(User invitedBy, Pageable pageable);

    /**
     * Find invitations for a user's groups
     */
    @Query("SELECT i FROM Invitation i WHERE i.group IN " +
            "(SELECT g FROM Group g JOIN g.members m WHERE m = :user) " +
            "ORDER BY i.createdAt DESC")
    List<Invitation> findForUserGroups(@Param("user") User user);

    /**
     * Check if user has pending invitation to group
     */
    @Query("SELECT COUNT(i) > 0 FROM Invitation i WHERE i.email = :email " +
            "AND i.group = :group AND i.status = 'PENDING' " +
            "AND i.expiresAt > CURRENT_TIMESTAMP")
    boolean hasPendingInvitation(@Param("email") String email, @Param("group") Group group);

    /**
     * Check if user is already member or has been invited to group
     */
    @Query("SELECT COUNT(i) > 0 FROM Invitation i WHERE i.email = :email " +
            "AND i.group = :group AND i.status IN ('PENDING', 'ACCEPTED')")
    boolean hasInvitationToGroup(@Param("email") String email, @Param("group") Group group);

    // ========== STATISTICS QUERIES ==========

    /**
     * Count invitations by status for a group
     */
    @Query("SELECT COUNT(i) FROM Invitation i WHERE i.group = :group AND i.status = :status")
    long countByGroupAndStatus(@Param("group") Group group, @Param("status") Invitation.InvitationStatus status);

    /**
     * Count invitations sent by a user
     */
    @Query("SELECT COUNT(i) FROM Invitation i WHERE i.invitedBy = :user")
    long countByInvitedBy(@Param("user") User user);

    /**
     * Get invitation acceptance rate for a group
     */
    @Query("SELECT " +
            "COUNT(CASE WHEN i.status = 'ACCEPTED' THEN 1 END) * 100.0 / COUNT(i) " +
            "FROM Invitation i WHERE i.group = :group")
    Double getAcceptanceRateByGroup(@Param("group") Group group);

    /**
     * Get invitation statistics for a user
     */
    @Query("SELECT i.status, COUNT(i) FROM Invitation i WHERE i.invitedBy = :user GROUP BY i.status")
    List<Object[]> getInvitationStatsByUser(@Param("user") User user);

    // ========== BULK OPERATIONS ==========

    /**
     * Mark expired invitations as expired
     */
    @Modifying
    @Query("UPDATE Invitation i SET i.status = 'EXPIRED' WHERE i.status = 'PENDING' " +
            "AND i.expiresAt < CURRENT_TIMESTAMP")
    int markExpiredInvitations();

    /**
     * Cancel pending invitations for a group
     */
    @Modifying
    @Query("UPDATE Invitation i SET i.status = 'CANCELLED' WHERE i.group = :group " +
            "AND i.status = 'PENDING'")
    int cancelPendingByGroup(@Param("group") Group group);

    /**
     * Delete old invitations (accepted, declined, expired older than specified
     * date)
     */
    @Modifying
    @Query("DELETE FROM Invitation i WHERE i.status IN ('ACCEPTED', 'DECLINED', 'EXPIRED') " +
            "AND i.createdAt < :cutoffDate")
    int deleteOldInvitations(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========== VALIDATION QUERIES ==========

    /**
     * Check if invitation token exists
     */
    boolean existsByInvitationToken(String invitationToken);

    /**
     * Find duplicate invitations (same email, group, and pending)
     */
    @Query("SELECT i FROM Invitation i WHERE i.email = :email AND i.group = :group " +
            "AND i.status = 'PENDING' AND i.expiresAt > CURRENT_TIMESTAMP " +
            "ORDER BY i.createdAt DESC")
    List<Invitation> findDuplicateInvitations(@Param("email") String email, @Param("group") Group group);

    // ========== COMPLEX BUSINESS QUERIES ==========

    /**
     * Find invitations that can be auto-accepted (user already exists and has
     * permission)
     */
    @Query("SELECT i FROM Invitation i WHERE i.status = 'PENDING' " +
            "AND i.expiresAt > CURRENT_TIMESTAMP " +
            "AND EXISTS (SELECT 1 FROM User u WHERE u.email = i.email) " +
            "ORDER BY i.createdAt DESC")
    List<Invitation> findAutoAcceptableInvitations();

    /**
     * Get invitation activity for a group (for activity feed)
     */
    @Query("SELECT i FROM Invitation i WHERE i.group = :group " +
            "AND i.createdAt >= :since " +
            "ORDER BY i.createdAt DESC")
    List<Invitation> getInvitationActivityForGroup(@Param("group") Group group,
            @Param("since") LocalDateTime since);
}
