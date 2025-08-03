package com.dasa.splitspends.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // ========== BASIC QUERIES ==========

    // Find all expenses in a given group, sorted by date (with pagination)
    Page<Expense> findByGroupOrderByDateDesc(Group group, Pageable pageable);

    // Find all expenses paid by a specific user
    List<Expense> findByPaidBy(User user);

    // Find expenses paid by user in specific group
    List<Expense> findByGroupAndPaidBy(Group group, User user);

    // Find expenses by group excluding deleted ones, ordered by date (with
    // pagination)
    Page<Expense> findByGroupAndStatusNotOrderByDateDesc(Group group, Expense.ExpenseStatus status, Pageable pageable);

    // Find expenses involving a specific user (either paid by them or split with
    // them)
    @Query("SELECT DISTINCT e FROM Expense e LEFT JOIN e.splits es " +
            "WHERE e.paidBy = :user OR es.user = :user " +
            "ORDER BY e.date DESC")
    List<Expense> findExpensesInvolvingUser(@Param("user") User user);

    // ========== BALANCE CALCULATIONS ==========

    // Get total amount spent in a group
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.group = :group")
    BigDecimal getTotalExpensesByGroup(@Param("group") Group group);

    // Get total amount spent in a group (alias for service compatibility)
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.group = :group AND e.status != 'DELETED'")
    BigDecimal getTotalAmountByGroup(@Param("group") Group group);

    // Get total settled amount in a group
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.group = :group AND e.status != 'DELETED' " +
            "AND NOT EXISTS (SELECT 1 FROM ExpenseSplit es WHERE es.expense = e AND es.settled = false)")
    BigDecimal getTotalSettledAmountByGroup(@Param("group") Group group);

    // Get total amount paid by a user in a group
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.group = :group AND e.paidBy = :user")
    BigDecimal getTotalPaidByUserInGroup(@Param("group") Group group, @Param("user") User user);

    // Get user's share of total group expenses
    @Query("SELECT COALESCE(SUM(es.shareAmount), 0) FROM ExpenseSplit es " +
            "JOIN es.expense e WHERE e.group = :group AND es.user = :user")
    BigDecimal getTotalOwedByUserInGroup(@Param("group") Group group, @Param("user") User user);

    // ========== ACTIVITY FEED ==========

    // Get recent expenses across all user's groups (for activity feed)
    @Query("SELECT e FROM Expense e JOIN e.group.members m " +
            "WHERE m = :user ORDER BY e.date DESC")
    Page<Expense> findRecentExpensesByUser(@Param("user") User user, Pageable pageable);

    // Get top 10 recent expenses for a group (for activity feed)
    @Query("SELECT e FROM Expense e WHERE e.group = :group AND e.status != :status " +
            "ORDER BY e.createdAt DESC")
    List<Expense> findTop10ByGroupAndStatusNotOrderByCreatedAtDesc(@Param("group") Group group,
            @Param("status") Expense.ExpenseStatus status);

    // Get expenses created after a certain date (for real-time updates)
    List<Expense> findByGroupAndDateAfterOrderByDateDesc(Group group, LocalDateTime after);

    // ========== SETTLE-UP CALCULATIONS ==========

    // Find unsettled expenses for a user in a group
    @Query("SELECT e FROM Expense e JOIN e.splits es " +
            "WHERE e.group = :group AND es.user = :user AND es.settled = false")
    List<Expense> findUnsettledExpensesByUserInGroup(@Param("group") Group group, @Param("user") User user);

    // Find all unsettled expenses in a group (for group management)
    @Query("SELECT DISTINCT e FROM Expense e JOIN e.splits es " +
            "WHERE e.group = :group AND es.settled = false AND e.status != 'DELETED'")
    List<Expense> findUnsettledExpensesByGroup(@Param("group") Group group);

    // Get expenses where user owes money to another user
    @Query("SELECT e FROM Expense e JOIN e.splits es " +
            "WHERE e.group = :group AND es.user = :debtor AND e.paidBy = :creditor AND es.settled = false")
    List<Expense> findExpensesOwedByUserToUser(@Param("group") Group group,
            @Param("debtor") User debtor,
            @Param("creditor") User creditor);

    // ========== ATTACHMENTS & RECEIPTS ==========

    // Find expenses with attachments/receipts
    List<Expense> findByGroupAndAttachmentUrlIsNotNull(Group group);

    // Find expenses without receipts (for reminder purposes)
    List<Expense> findByGroupAndAttachmentUrlIsNull(Group group);

    // ========== DATE RANGE QUERIES ==========

    // Get expenses in date range for reporting
    List<Expense> findByGroupAndDateBetweenOrderByDateDesc(Group group,
            LocalDateTime startDate,
            LocalDateTime endDate);

    // ========== STATISTICS & ANALYTICS ==========

    // Get expense count by group
    Long countByGroup(Group group);

    // Count expenses by group excluding deleted ones
    Long countByGroupAndStatusNot(Group group, Expense.ExpenseStatus status);

    // Get average expense amount in group
    @Query("SELECT AVG(e.amount) FROM Expense e WHERE e.group = :group")
    BigDecimal getAverageExpenseByGroup(@Param("group") Group group);

    // Get average expense amount in group (alias for service compatibility)
    @Query("SELECT COALESCE(AVG(e.amount), 0) FROM Expense e WHERE e.group = :group AND e.status != 'DELETED'")
    BigDecimal getAverageExpenseAmountByGroup(@Param("group") Group group);

    // Get largest expense in group
    @Query("SELECT e FROM Expense e WHERE e.group = :group ORDER BY e.amount DESC")
    Page<Expense> findLargestExpensesByGroup(@Param("group") Group group, Pageable pageable);
}