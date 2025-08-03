package com.dasa.splitspends.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.ExpenseSplit;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.ExpenseRepository;
import com.dasa.splitspends.repository.ExpenseSplitRepository;
import com.dasa.splitspends.repository.GroupRepository;
import com.dasa.splitspends.repository.UserRepository;

@Service
@Transactional
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseSplitRepository expenseSplitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @SuppressWarnings("unused")
    @Autowired
    private ExpenseSplitService expenseSplitService;

    // ========== EXPENSE CRUD OPERATIONS ==========

    /**
     * Create a new expense with equal splits
     */
    public Expense createExpenseWithEqualSplits(Long groupId, Long paidByUserId,
            String description, BigDecimal amount,
            List<Long> participantUserIds,
            Expense.ExpenseCategory category) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User paidBy = userRepository.findById(paidByUserId)
                .orElseThrow(() -> new IllegalArgumentException("Paid by user not found"));

        // Validate all participants exist and are in the group
        List<User> participants = validateParticipants(participantUserIds, group);

        // Create expense
        Expense expense = Expense.builder()
                .group(group)
                .description(description)
                .amount(amount)
                .date(LocalDateTime.now())
                .paidBy(paidBy)
                .category(category)
                .status(Expense.ExpenseStatus.CONFIRMED)
                .createdBy(paidBy)
                .build();

        expense = expenseRepository.save(expense);

        // Create equal splits
        createEqualSplits(expense, participants);

        return expense;
    }

    /**
     * Create an expense with custom splits
     */
    public Expense createExpenseWithCustomSplits(Long groupId, Long paidByUserId,
            String description, BigDecimal amount,
            Map<Long, BigDecimal> userAmountMap,
            Expense.ExpenseCategory category) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User paidBy = userRepository.findById(paidByUserId)
                .orElseThrow(() -> new IllegalArgumentException("Paid by user not found"));

        // Validate split amounts total equals expense amount
        BigDecimal totalSplits = userAmountMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (amount.subtract(totalSplits).abs().compareTo(new BigDecimal("0.01")) > 0) {
            throw new IllegalArgumentException("Split amounts must equal total expense amount");
        }

        // Create expense
        Expense expense = Expense.builder()
                .group(group)
                .description(description)
                .amount(amount)
                .date(LocalDateTime.now())
                .paidBy(paidBy)
                .category(category)
                .status(Expense.ExpenseStatus.CONFIRMED)
                .createdBy(paidBy)
                .build();

        expense = expenseRepository.save(expense);

        // Create custom splits
        createCustomSplits(expense, userAmountMap, group);

        return expense;
    }

    /**
     * Update an existing expense
     */
    public Expense updateExpense(Long expenseId, String description, BigDecimal amount,
            Expense.ExpenseCategory category, String notes, User updatedBy) {

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        // Check if expense can be modified (not fully settled)
        if (expense.isFullySettled()) {
            throw new IllegalStateException("Cannot modify fully settled expense");
        }

        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setNotes(notes);
        expense.setUpdatedBy(updatedBy);

        // Recalculate splits if amount changed
        if (!expense.getAmount().equals(amount)) {
            recalculateSplits(expense);
        }

        return expenseRepository.save(expense);
    }

    /**
     * Delete an expense (soft delete)
     */
    public void deleteExpense(Long expenseId, User deletedBy) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        if (expense.isFullySettled()) {
            throw new IllegalStateException("Cannot delete fully settled expense");
        }

        expense.setStatus(Expense.ExpenseStatus.DELETED);
        expense.setUpdatedBy(deletedBy);
        expenseRepository.save(expense);
    }

    // ========== EXPENSE RETRIEVAL ==========

    /**
     * Get expenses for a group with pagination
     */
    public Page<Expense> getGroupExpenses(Long groupId, Pageable pageable) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        return expenseRepository.findByGroupAndStatusNotOrderByDateDesc(group,
                Expense.ExpenseStatus.DELETED,
                pageable);
    }

    /**
     * Get expenses involving a specific user
     */
    public List<Expense> getUserExpenses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return expenseRepository.findExpensesInvolvingUser(user);
    }

    /**
     * Get recent expenses for activity feed
     */
    public List<Expense> getRecentExpenses(Long groupId, int limit) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        return expenseRepository.findTop10ByGroupAndStatusNotOrderByCreatedAtDesc(group,
                Expense.ExpenseStatus.DELETED);
    }

    // ========== ATTACHMENT HANDLING ==========

    /**
     * Add receipt attachment to expense
     */
    public Expense addAttachment(Long expenseId, MultipartFile file) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        // TODO: Implement file upload to cloud storage (AWS S3, etc.)
        // For now, just store filename
        expense.setAttachmentFilename(file.getOriginalFilename());
        expense.setAttachmentUrl("/uploads/" + expenseId + "_" + file.getOriginalFilename());

        return expenseRepository.save(expense);
    }

    // ========== SPLIT MANAGEMENT ==========

    /**
     * Create equal splits for an expense
     */
    private void createEqualSplits(Expense expense, List<User> participants) {
        BigDecimal shareAmount = expense.getAmount()
                .divide(BigDecimal.valueOf(participants.size()), 2, RoundingMode.HALF_UP);

        BigDecimal totalAssigned = BigDecimal.ZERO;

        for (int i = 0; i < participants.size(); i++) {
            User participant = participants.get(i);
            BigDecimal amount = shareAmount;

            // Handle rounding - assign remainder to last participant
            if (i == participants.size() - 1) {
                amount = expense.getAmount().subtract(totalAssigned);
            }

            ExpenseSplit split = ExpenseSplit.builder()
                    .expense(expense)
                    .user(participant)
                    .shareAmount(amount)
                    .splitType(ExpenseSplit.SplitType.EQUAL)
                    .settled(false)
                    .build();

            expense.addSplit(split);
            totalAssigned = totalAssigned.add(amount);
        }

        expenseSplitRepository.saveAll(expense.getSplits());
    }

    /**
     * Create custom splits for an expense
     */
    private void createCustomSplits(Expense expense, Map<Long, BigDecimal> userAmountMap, Group group) {
        for (Map.Entry<Long, BigDecimal> entry : userAmountMap.entrySet()) {
            User user = userRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + entry.getKey()));

            // Validate user is in the group
            if (!group.getMembers().contains(user)) {
                throw new IllegalArgumentException("User is not a member of the group");
            }

            ExpenseSplit split = ExpenseSplit.builder()
                    .expense(expense)
                    .user(user)
                    .shareAmount(entry.getValue())
                    .splitType(ExpenseSplit.SplitType.EXACT_AMOUNT)
                    .settled(false)
                    .build();

            expense.addSplit(split);
        }

        expenseSplitRepository.saveAll(expense.getSplits());
    }

    /**
     * Recalculate splits when expense amount changes
     */
    private void recalculateSplits(Expense expense) {
        List<ExpenseSplit> currentSplits = new ArrayList<>(expense.getSplits());

        // If all splits are equal type, redistribute equally
        boolean allEqualSplits = currentSplits.stream()
                .allMatch(split -> split.getSplitType() == ExpenseSplit.SplitType.EQUAL);

        if (allEqualSplits) {
            // Clear existing splits and recreate equal splits
            expense.getSplits().clear();
            List<User> participants = currentSplits.stream()
                    .map(ExpenseSplit::getUser)
                    .collect(Collectors.toList());
            createEqualSplits(expense, participants);
        } else {
            // For custom splits, maintain proportions
            BigDecimal oldTotal = currentSplits.stream()
                    .map(ExpenseSplit::getShareAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal ratio = expense.getAmount().divide(oldTotal, 4, RoundingMode.HALF_UP);

            for (ExpenseSplit split : currentSplits) {
                BigDecimal newAmount = split.getShareAmount().multiply(ratio)
                        .setScale(2, RoundingMode.HALF_UP);
                split.setShareAmount(newAmount);
            }
        }
    }

    /**
     * Validate participants exist and are group members
     */
    private List<User> validateParticipants(List<Long> participantUserIds, Group group) {
        List<User> participants = userRepository.findAllById(participantUserIds);

        if (participants.size() != participantUserIds.size()) {
            throw new IllegalArgumentException("Some participants not found");
        }

        for (User participant : participants) {
            if (!group.getMembers().contains(participant)) {
                throw new IllegalArgumentException("User " + participant.getName() +
                        " is not a member of the group");
            }
        }

        return participants;
    }

    // ========== STATISTICS & ANALYTICS ==========

    /**
     * Get expense statistics for a group
     */
    public ExpenseStats getGroupExpenseStats(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        return ExpenseStats.builder()
                .totalExpenses(expenseRepository.countByGroupAndStatusNot(group, Expense.ExpenseStatus.DELETED))
                .totalAmount(expenseRepository.getTotalAmountByGroup(group))
                .settledAmount(expenseRepository.getTotalSettledAmountByGroup(group))
                .averageExpenseAmount(expenseRepository.getAverageExpenseAmountByGroup(group))
                .build();
    }

    // ========== DTO CLASSES ==========

    @lombok.Builder
    @lombok.Data
    public static class ExpenseStats {
        private Long totalExpenses;
        private BigDecimal totalAmount;
        private BigDecimal settledAmount;
        private BigDecimal averageExpenseAmount;

        public BigDecimal getUnsettledAmount() {
            if (totalAmount == null || settledAmount == null)
                return BigDecimal.ZERO;
            return totalAmount.subtract(settledAmount);
        }

        public double getSettlementPercentage() {
            if (totalAmount == null || totalAmount.equals(BigDecimal.ZERO))
                return 0.0;
            return settledAmount.divide(totalAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }
    }
}
