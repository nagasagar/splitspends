package com.dasa.splitspends.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.SettleUp;
import com.dasa.splitspends.entity.User;

@Repository
public interface SettleUpRepository extends JpaRepository<SettleUp, Long> {

    // ========== BASIC QUERIES ==========

    /**
     * Find settlements by group
     */
    List<SettleUp> findByGroupOrderByCreatedAtDesc(Group group);

    /**
     * Find settlements by payer
     */
    List<SettleUp> findByPayerOrderByCreatedAtDesc(User payer);

    /**
     * Find settlements by payee
     */
    List<SettleUp> findByPayeeOrderByCreatedAtDesc(User payee);

    /**
     * Find settlements by status
     */
    List<SettleUp> findByStatusOrderByCreatedAtDesc(SettleUp.SettlementStatus status);

    // ========== USER-SPECIFIC QUERIES ==========

    /**
     * Find all settlements where user is either payer or payee
     */
    @Query("SELECT s FROM SettleUp s WHERE s.payer = :user OR s.payee = :user ORDER BY s.createdAt DESC")
    List<SettleUp> findByUserInvolved(@Param("user") User user);

    /**
     * Find pending settlements for a user (where they need to take action)
     */
    @Query("SELECT s FROM SettleUp s WHERE (s.payer = :user OR s.payee = :user) " +
            "AND s.status = 'PENDING' ORDER BY s.createdAt DESC")
    List<SettleUp> findPendingByUser(@Param("user") User user);

    /**
     * Find settlements where user is payer and status is pending
     */
    @Query("SELECT s FROM SettleUp s WHERE s.payer = :user AND s.status = 'PENDING' " +
            "ORDER BY s.createdAt DESC")
    List<SettleUp> findPendingByPayer(@Param("user") User user);

    /**
     * Find settlements where user is payee and status is pending
     */
    @Query("SELECT s FROM SettleUp s WHERE s.payee = :user AND s.status = 'PENDING' " +
            "ORDER BY s.createdAt DESC")
    List<SettleUp> findPendingByPayee(@Param("user") User user);

    // ========== GROUP-SPECIFIC QUERIES ==========

    /**
     * Find settlements in a group with pagination
     */
    Page<SettleUp> findByGroupOrderByCreatedAtDesc(Group group, Pageable pageable);

    /**
     * Find pending settlements in a group
     */
    @Query("SELECT s FROM SettleUp s WHERE s.group = :group AND s.status = 'PENDING' " +
            "ORDER BY s.createdAt DESC")
    List<SettleUp> findPendingByGroup(@Param("group") Group group);

    /**
     * Find completed settlements in a group
     */
    @Query("SELECT s FROM SettleUp s WHERE s.group = :group AND s.status = 'COMPLETED' " +
            "ORDER BY s.confirmedAt DESC")
    List<SettleUp> findCompletedByGroup(@Param("group") Group group);

    /**
     * Find settlements between two specific users in a group
     */
    @Query("SELECT s FROM SettleUp s WHERE s.group = :group " +
            "AND ((s.payer = :user1 AND s.payee = :user2) OR (s.payer = :user2 AND s.payee = :user1)) " +
            "ORDER BY s.createdAt DESC")
    List<SettleUp> findBetweenUsersInGroup(@Param("group") Group group,
            @Param("user1") User user1,
            @Param("user2") User user2);

    // ========== AMOUNT-BASED QUERIES ==========

    /**
     * Find settlements by amount range
     */
    @Query("SELECT s FROM SettleUp s WHERE s.amount BETWEEN :minAmount AND :maxAmount " +
            "ORDER BY s.amount DESC")
    List<SettleUp> findByAmountRange(@Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount);

    /**
     * Get total settlement amount for a user (as payer)
     */
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM SettleUp s WHERE s.payer = :user AND s.status = 'COMPLETED'")
    BigDecimal getTotalPaidByUser(@Param("user") User user);

    /**
     * Get total settlement amount received by a user (as payee)
     */
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM SettleUp s WHERE s.payee = :user AND s.status = 'COMPLETED'")
    BigDecimal getTotalReceivedByUser(@Param("user") User user);

    /**
     * Get pending settlement amount for a user (as payer)
     */
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM SettleUp s WHERE s.payer = :user AND s.status = 'PENDING'")
    BigDecimal getPendingAmountByPayer(@Param("user") User user);

    // ========== DATE-BASED QUERIES ==========

    /**
     * Find settlements created within date range
     */
    @Query("SELECT s FROM SettleUp s WHERE s.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY s.createdAt DESC")
    List<SettleUp> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find settlements confirmed within date range
     */
    @Query("SELECT s FROM SettleUp s WHERE s.confirmedAt BETWEEN :startDate AND :endDate " +
            "ORDER BY s.confirmedAt DESC")
    List<SettleUp> findConfirmedInDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find recent settlements for a group (last 30 days)
     */
    @Query("SELECT s FROM SettleUp s WHERE s.group = :group " +
            "AND s.createdAt >= :thirtyDaysAgo ORDER BY s.createdAt DESC")
    List<SettleUp> findRecentByGroup(@Param("group") Group group,
            @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

    // ========== STATISTICS QUERIES ==========

    /**
     * Count settlements by status for a group
     */
    @Query("SELECT COUNT(s) FROM SettleUp s WHERE s.group = :group AND s.status = :status")
    long countByGroupAndStatus(@Param("group") Group group, @Param("status") SettleUp.SettlementStatus status);

    /**
     * Count pending settlements for a user
     */
    @Query("SELECT COUNT(s) FROM SettleUp s WHERE (s.payer = :user OR s.payee = :user) AND s.status = 'PENDING'")
    long countPendingByUser(@Param("user") User user);

    /**
     * Get average settlement amount for a group
     */
    @Query("SELECT AVG(s.amount) FROM SettleUp s WHERE s.group = :group AND s.status = 'COMPLETED'")
    Optional<BigDecimal> getAverageSettlementAmountByGroup(@Param("group") Group group);

    // ========== PAYMENT METHOD QUERIES ==========

    /**
     * Find settlements by payment method
     */
    List<SettleUp> findByPaymentMethodOrderByCreatedAtDesc(SettleUp.PaymentMethod paymentMethod);

    /**
     * Find settlements by payment method in a group
     */
    @Query("SELECT s FROM SettleUp s WHERE s.group = :group AND s.paymentMethod = :paymentMethod " +
            "ORDER BY s.createdAt DESC")
    List<SettleUp> findByGroupAndPaymentMethod(@Param("group") Group group,
            @Param("paymentMethod") SettleUp.PaymentMethod paymentMethod);

    // ========== EXTERNAL TRANSACTION QUERIES ==========

    /**
     * Find settlement by external transaction ID
     */
    Optional<SettleUp> findByExternalTransactionId(String externalTransactionId);

    /**
     * Check if external transaction ID exists
     */
    boolean existsByExternalTransactionId(String externalTransactionId);

    // ========== COMPLEX BUSINESS QUERIES ==========

    /**
     * Find settlements that need reminder (pending for more than X days)
     */
    @Query("SELECT s FROM SettleUp s WHERE s.status = 'PENDING' " +
            "AND s.createdAt < :reminderThreshold ORDER BY s.createdAt ASC")
    List<SettleUp> findSettlementsNeedingReminder(@Param("reminderThreshold") LocalDateTime reminderThreshold);

    /**
     * Find expired pending settlements (pending for more than 30 days)
     */
    @Query("SELECT s FROM SettleUp s WHERE s.status = 'PENDING' " +
            "AND s.createdAt < :expirationThreshold ORDER BY s.createdAt ASC")
    List<SettleUp> findExpiredPendingSettlements(@Param("expirationThreshold") LocalDateTime expirationThreshold);

    /**
     * Get settlement summary for a user in a group
     */
    @Query("SELECT s FROM SettleUp s WHERE s.group = :group " +
            "AND (s.payer = :user OR s.payee = :user) " +
            "ORDER BY s.createdAt DESC")
    List<SettleUp> getUserSettlementSummaryInGroup(@Param("group") Group group, @Param("user") User user);
}
