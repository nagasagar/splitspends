package com.dasa.splitspends.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.ExpenseSplit;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;

public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {

    // ========== SETTLEMENT QUERIES ==========

    // Find all unsettled splits for a user (across all groups)
    List<ExpenseSplit> findByUserAndSettledFalse(User user);

    // Find unsettled splits for a user in a specific group
    @Query("SELECT es FROM ExpenseSplit es JOIN es.expense e " +
            "WHERE e.group = :group AND es.user = :user AND es.settled = false")
    List<ExpenseSplit> findUnsettledSplitsByUserInGroup(@Param("group") Group group, @Param("user") User user);

    // Find all splits for a specific expense
    List<ExpenseSplit> findByExpense(Expense expense);

    // Find settled splits for a user in a group
    @Query("SELECT es FROM ExpenseSplit es JOIN es.expense e " +
            "WHERE e.group = :group AND es.user = :user AND es.settled = true")
    List<ExpenseSplit> findSettledSplitsByUserInGroup(@Param("group") Group group, @Param("user") User user);

    // Find unsettled splits for a user where expense date is before specified date
    // (for overdue tracking)
    @Query("SELECT es FROM ExpenseSplit es JOIN es.expense e " +
            "WHERE es.user.id = :userId AND es.settled = false AND e.date < :beforeDate " +
            "ORDER BY e.date ASC")
    List<ExpenseSplit> findUnsettledSplitsByUserAndExpenseDateBefore(
            @Param("userId") Long userId,
            @Param("beforeDate") LocalDateTime beforeDate);

    // ========== BALANCE CALCULATIONS ==========

    // Get total amount a user owes in a group (unsettled only)
    @Query("SELECT COALESCE(SUM(es.shareAmount), 0) FROM ExpenseSplit es " +
            "JOIN es.expense e WHERE e.group = :group AND es.user = :user AND es.settled = false")
    BigDecimal getTotalUnsettledAmountByUserInGroup(@Param("group") Group group, @Param("user") User user);

    // Get total amount a user has paid (through their expense splits)
    @Query("SELECT COALESCE(SUM(es.shareAmount), 0) FROM ExpenseSplit es " +
            "JOIN es.expense e WHERE e.group = :group AND es.user = :user AND es.settled = true")
    BigDecimal getTotalSettledAmountByUserInGroup(@Param("group") Group group, @Param("user") User user);

    // Get net balance between two users in a group
    @Query("SELECT COALESCE(SUM(CASE " +
            "WHEN e.paidBy = :user1 AND es.user = :user2 AND es.settled = false THEN es.shareAmount " +
            "WHEN e.paidBy = :user2 AND es.user = :user1 AND es.settled = false THEN -es.shareAmount " +
            "ELSE 0 END), 0) " +
            "FROM ExpenseSplit es JOIN es.expense e " +
            "WHERE e.group = :group AND ((e.paidBy = :user1 AND es.user = :user2) OR (e.paidBy = :user2 AND es.user = :user1))")
    BigDecimal getNetBalanceBetweenUsers(@Param("group") Group group,
            @Param("user1") User user1,
            @Param("user2") User user2);

    // Get all amounts a user owes to other users in a group
    @Query("SELECT es FROM ExpenseSplit es JOIN es.expense e " +
            "WHERE e.group = :group AND es.user = :debtor AND e.paidBy = :creditor AND es.settled = false")
    List<ExpenseSplit> findAmountsOwedByUserToUser(@Param("group") Group group,
            @Param("debtor") User debtor,
            @Param("creditor") User creditor);

    // ========== SETTLEMENT OPERATIONS ==========

    // Mark specific splits as settled
    @Modifying
    @Query("UPDATE ExpenseSplit es SET es.settled = true, es.settledAt = CURRENT_TIMESTAMP " +
            "WHERE es.id IN :splitIds")
    void markSplitsAsSettledByIds(@Param("splitIds") List<Long> splitIds);

    // Mark all splits between two users as settled
    @Modifying
    @Query("UPDATE ExpenseSplit es SET es.settled = true, es.settledAt = CURRENT_TIMESTAMP " +
            "WHERE es.id IN (SELECT es2.id FROM ExpenseSplit es2 JOIN es2.expense e " +
            "WHERE e.group = :group AND es2.user = :debtor AND e.paidBy = :creditor AND es2.settled = false)")
    void settleAmountsBetweenUsers(@Param("group") Group group,
            @Param("debtor") User debtor,
            @Param("creditor") User creditor);

    // Mark all user's splits in a group as settled
    @Modifying
    @Query("UPDATE ExpenseSplit es SET es.settled = true, es.settledAt = CURRENT_TIMESTAMP " +
            "WHERE es.user = :user AND es.expense IN " +
            "(SELECT e FROM Expense e WHERE e.group = :group) AND es.settled = false")
    void settleAllUserSplitsInGroup(@Param("group") Group group, @Param("user") User user);

    // ========== STATISTICS & ANALYTICS ==========

    // Count unsettled splits in a group
    @Query("SELECT COUNT(es) FROM ExpenseSplit es JOIN es.expense e " +
            "WHERE e.group = :group AND es.settled = false")
    Long countUnsettledSplitsByGroup(@Param("group") Group group);

    // Count total splits for a user (for activity stats)
    Long countByUser(User user);

    // Get average split amount for a user in a group
    @Query("SELECT AVG(es.shareAmount) FROM ExpenseSplit es JOIN es.expense e " +
            "WHERE e.group = :group AND es.user = :user")
    BigDecimal getAverageSplitAmountByUserInGroup(@Param("group") Group group, @Param("user") User user);

    // ========== VALIDATION & CHECKS ==========

    // Check if expense has any unsettled splits
    @Query("SELECT CASE WHEN COUNT(es) > 0 THEN true ELSE false END " +
            "FROM ExpenseSplit es WHERE es.expense = :expense AND es.settled = false")
    boolean hasUnsettledSplits(@Param("expense") Expense expense);

    // Validate split amounts total equals expense amount
    @Query("SELECT COALESCE(SUM(es.shareAmount), 0) FROM ExpenseSplit es WHERE es.expense = :expense")
    BigDecimal getTotalSplitAmountForExpense(@Param("expense") Expense expense);

    // ========== REPORTING ==========

    // Find splits by amount range (for expense analysis)
    @Query("SELECT es FROM ExpenseSplit es JOIN es.expense e " +
            "WHERE e.group = :group AND es.shareAmount BETWEEN :minAmount AND :maxAmount")
    List<ExpenseSplit> findSplitsByAmountRange(@Param("group") Group group,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount);
}
