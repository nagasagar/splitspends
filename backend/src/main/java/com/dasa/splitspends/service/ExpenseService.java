
package com.dasa.splitspends.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.dasa.splitspends.dto.ExpenseStats;
import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.User;

public interface ExpenseService {

    /**
     * Get expense statistics for a specific user.
     *
     * @param userId the user ID
     * @return ExpenseStats for the user
     */
    ExpenseStats getExpenseStatsForUser(Long userId);

    /**
     * Create a new expense with equal splits among participants.
     * 
     * @param groupId            the group ID
     * @param paidByUserId       the user who paid
     * @param description        expense description
     * @param amount             total expense amount
     * @param participantUserIds list of participant user IDs
     * @param category           expense category
     * @return the created Expense
     */
    Expense createExpenseWithEqualSplits(Long groupId, Long paidByUserId, String description, BigDecimal amount,
            List<Long> participantUserIds, Expense.ExpenseCategory category);

    /**
     * Create a new expense with custom split amounts for each participant.
     * 
     * @param groupId       the group ID
     * @param paidByUserId  the user who paid
     * @param description   expense description
     * @param amount        total expense amount
     * @param userAmountMap map of user ID to split amount
     * @param category      expense category
     * @return the created Expense
     */
    Expense createExpenseWithCustomSplits(Long groupId, Long paidByUserId, String description, BigDecimal amount,
            Map<Long, BigDecimal> userAmountMap, Expense.ExpenseCategory category);

    /**
     * Update an existing expense.
     * 
     * @param expenseId   the expense ID
     * @param description new description
     * @param amount      new amount
     * @param category    new category
     * @param notes       update notes
     * @param updatedBy   the user making the update
     * @return the updated Expense
     */
    Expense updateExpense(Long expenseId, String description, BigDecimal amount, Expense.ExpenseCategory category,
            String notes, User updatedBy);

    /**
     * Delete an expense (soft delete).
     * 
     * @param expenseId the expense ID
     * @param deletedBy the user performing the delete
     */
    void deleteExpense(Long expenseId, User deletedBy);

    /**
     * Get paginated expenses for a group.
     * 
     * @param groupId  the group ID
     * @param pageable pagination info
     * @return page of expenses
     */
    Page<Expense> getGroupExpenses(Long groupId, Pageable pageable);

    /**
     * Get all expenses involving a specific user.
     * 
     * @param userId the user ID
     * @return list of expenses
     */
    List<Expense> getUserExpenses(Long userId);

    /**
     * Get recent expenses for a group (for activity feed).
     * 
     * @param groupId the group ID
     * @param limit   max number of expenses
     * @return list of recent expenses
     */
    List<Expense> getRecentExpenses(Long groupId, int limit);

    /**
     * Add a receipt attachment to an expense.
     * 
     * @param expenseId the expense ID
     * @param file      the attachment file
     * @return the updated Expense
     */
    Expense addAttachment(Long expenseId, MultipartFile file);

    /**
     * Get statistics for a group (total, settled, average, etc.).
     * 
     * @param groupId the group ID
     * @return ExpenseStats for the group
     */
    ExpenseStats getGroupExpenseStats(Long groupId);
}
