package com.dasa.splitspends.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dasa.splitspends.entity.ExpenseSplit;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.ExpenseSplitRepository;
import com.dasa.splitspends.repository.GroupRepository;
import com.dasa.splitspends.repository.UserRepository;

@Service
@Transactional
public class ExpenseSplitService {

    @Autowired
    private ExpenseSplitRepository expenseSplitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Check if a split is overdue based on configurable days
     * This is better in service as it might involve configuration
     */
    public boolean isOverdue(ExpenseSplit split, int overdueThresholdDays) {
        if (split.isSettled() || split.getExpense() == null || split.getExpense().getDate() == null) {
            return false;
        }
        return split.getExpense().getDate().isBefore(LocalDateTime.now().minusDays(overdueThresholdDays));
    }

    /**
     * Default overdue check (30 days)
     */
    public boolean isOverdue(ExpenseSplit split) {
        return isOverdue(split, 30);
    }

    /**
     * Settle a split with full audit trail
     */
    public ExpenseSplit settleSplit(Long splitId, User settledByUser, String note) {
        ExpenseSplit split = expenseSplitRepository.findById(splitId)
                .orElseThrow(() -> new IllegalArgumentException("Split not found"));

        split.markAsSettled(settledByUser, note);
        return expenseSplitRepository.save(split);
    }

    /**
     * Unsettle a split
     */
    public ExpenseSplit unsettleSplit(Long splitId) {
        ExpenseSplit split = expenseSplitRepository.findById(splitId)
                .orElseThrow(() -> new IllegalArgumentException("Split not found"));

        split.markAsUnsettled();
        return expenseSplitRepository.save(split);
    }

    /**
     * Bulk settle multiple splits
     */
    public void bulkSettleSplits(List<Long> splitIds, User settledByUser, String note) {
        List<ExpenseSplit> splits = expenseSplitRepository.findAllById(splitIds);

        splits.forEach(split -> split.markAsSettled(settledByUser, note));
        expenseSplitRepository.saveAll(splits);
    }

    /**
     * Calculate total debt for a user in a group
     */
    public BigDecimal calculateUserDebtInGroup(Long userId, Long groupId) {
        com.dasa.splitspends.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        com.dasa.splitspends.entity.Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        return expenseSplitRepository.getTotalUnsettledAmountByUserInGroup(group, user);
    }

    /**
     * Calculate net balance between two users
     */
    public BigDecimal calculateNetBalanceBetweenUsers(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new IllegalArgumentException("User 1 not found"));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new IllegalArgumentException("User 2 not found"));
        // If a group is required, you need to provide a groupId parameter and fetch the
        // group as well.
        // For now, assuming group is not required or can be null:
        return expenseSplitRepository.getNetBalanceBetweenUsers(null, user1, user2);
    }

    /**
     * Get overdue splits for a user
     */
    public List<ExpenseSplit> getOverdueSplitsForUser(Long userId, int overdueThresholdDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(overdueThresholdDays);
        return expenseSplitRepository.findUnsettledSplitsByUserAndExpenseDateBefore(userId, cutoffDate);
    }

    /**
     * Validate split amounts for an expense
     */
    public boolean validateSplitAmounts(List<ExpenseSplit> splits, BigDecimal totalExpenseAmount) {
        BigDecimal totalSplitAmount = splits.stream()
                .map(ExpenseSplit::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Allow small rounding differences (within 1 cent)
        return totalSplitAmount.subtract(totalExpenseAmount).abs()
                .compareTo(new BigDecimal("0.01")) <= 0;
    }
}
