// ...existing code...

package com.dasa.splitspends.service.impl;

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

import com.dasa.splitspends.dto.ExpenseStats;
import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.ExpenseSplit;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.ExpenseRepository;
import com.dasa.splitspends.repository.ExpenseSplitRepository;
import com.dasa.splitspends.repository.GroupRepository;
import com.dasa.splitspends.repository.UserRepository;
import com.dasa.splitspends.service.ExpenseService;
import com.dasa.splitspends.service.ExpenseSplitService;

@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

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

        @Override
        public Expense createExpenseWithEqualSplits(Long groupId, Long paidByUserId,
                        String description, BigDecimal amount,
                        List<Long> participantUserIds,
                        Expense.ExpenseCategory category) {
                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
                User paidBy = userRepository.findById(paidByUserId)
                                .orElseThrow(() -> new IllegalArgumentException("Paid by user not found"));
                List<User> participants = validateParticipants(participantUserIds, group);
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
                createEqualSplits(expense, participants);
                return expense;
        }

        @Override
        public Expense createExpenseWithCustomSplits(Long groupId, Long paidByUserId,
                        String description, BigDecimal amount,
                        Map<Long, BigDecimal> userAmountMap,
                        Expense.ExpenseCategory category) {
                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
                User paidBy = userRepository.findById(paidByUserId)
                                .orElseThrow(() -> new IllegalArgumentException("Paid by user not found"));
                BigDecimal totalSplits = userAmountMap.values().stream()
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (amount.subtract(totalSplits).abs().compareTo(new BigDecimal("0.01")) > 0) {
                        throw new IllegalArgumentException("Split amounts must equal total expense amount");
                }
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
                createCustomSplits(expense, userAmountMap, group);
                return expense;
        }

        @Override
        public Expense updateExpense(Long expenseId, String description, BigDecimal amount,
                        Expense.ExpenseCategory category, String notes, User updatedBy) {
                Expense expense = expenseRepository.findById(expenseId)
                                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
                if (expense.isFullySettled()) {
                        throw new IllegalStateException("Cannot modify fully settled expense");
                }
                expense.setDescription(description);
                expense.setAmount(amount);
                expense.setCategory(category);
                expense.setNotes(notes);
                expense.setUpdatedBy(updatedBy);
                if (!expense.getAmount().equals(amount)) {
                        recalculateSplits(expense);
                }
                return expenseRepository.save(expense);
        }

        @Override
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

        @Override
        public Page<Expense> getGroupExpenses(Long groupId, Pageable pageable) {
                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
                return expenseRepository.findByGroupAndStatusNotOrderByDateDesc(group,
                                Expense.ExpenseStatus.DELETED,
                                pageable);
        }

        @Override
        public List<Expense> getUserExpenses(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                return expenseRepository.findExpensesInvolvingUser(user);
        }

        @Override
        public List<Expense> getRecentExpenses(Long groupId, int limit) {
                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
                return expenseRepository.findTop10ByGroupAndStatusNotOrderByCreatedAtDesc(group,
                                Expense.ExpenseStatus.DELETED);
        }

        @Override
        public Expense addAttachment(Long expenseId, MultipartFile file) {
                Expense expense = expenseRepository.findById(expenseId)
                                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
                // TODO: Implement file upload to cloud storage (AWS S3, etc.)
                // For now, just store filename
                expense.setAttachmentFilename(file.getOriginalFilename());
                expense.setAttachmentUrl("/uploads/" + expenseId + "_" + file.getOriginalFilename());
                return expenseRepository.save(expense);
        }

        @Override
        public ExpenseStats getGroupExpenseStats(Long groupId) {
                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
                return ExpenseStats.builder()
                                .totalExpenses(expenseRepository.countByGroupAndStatusNot(group,
                                                Expense.ExpenseStatus.DELETED))
                                .totalAmount(expenseRepository.getTotalAmountByGroup(group))
                                .settledAmount(expenseRepository.getTotalSettledAmountByGroup(group))
                                .averageExpenseAmount(expenseRepository.getAverageExpenseAmountByGroup(group))
                                .build();
        }

        @Override
        public ExpenseStats getExpenseStatsForUser(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                List<Expense> expenses = expenseRepository.findExpensesInvolvingUser(user);
                BigDecimal totalAmount = expenses.stream()
                                .map(Expense::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                long totalExpenses = expenses.size();
                BigDecimal settledAmount = expenses.stream()
                                .filter(Expense::isFullySettled)
                                .map(Expense::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal averageExpenseAmount = totalExpenses > 0
                                ? totalAmount.divide(BigDecimal.valueOf(totalExpenses), 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;
                return ExpenseStats.builder()
                                .totalExpenses(totalExpenses)
                                .totalAmount(totalAmount)
                                .settledAmount(settledAmount)
                                .averageExpenseAmount(averageExpenseAmount)
                                .build();
        }

        // ========== PRIVATE HELPERS ========== //

        private void createEqualSplits(Expense expense, List<User> participants) {
                BigDecimal shareAmount = expense.getAmount()
                                .divide(BigDecimal.valueOf(participants.size()), 2, RoundingMode.HALF_UP);
                BigDecimal totalAssigned = BigDecimal.ZERO;
                for (int i = 0; i < participants.size(); i++) {
                        User participant = participants.get(i);
                        BigDecimal amount = shareAmount;
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

        private void createCustomSplits(Expense expense, Map<Long, BigDecimal> userAmountMap, Group group) {
                for (Map.Entry<Long, BigDecimal> entry : userAmountMap.entrySet()) {
                        User user = userRepository.findById(entry.getKey())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "User not found: " + entry.getKey()));
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

        private void recalculateSplits(Expense expense) {
                List<ExpenseSplit> currentSplits = new ArrayList<>(expense.getSplits());
                boolean allEqualSplits = currentSplits.stream()
                                .allMatch(split -> split.getSplitType() == ExpenseSplit.SplitType.EQUAL);
                if (allEqualSplits) {
                        expense.getSplits().clear();
                        List<User> participants = currentSplits.stream()
                                        .map(ExpenseSplit::getUser)
                                        .collect(Collectors.toList());
                        createEqualSplits(expense, participants);
                } else {
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

        @Override
        public Expense createExpenseWithPercentageSplits(Long groupId, Long paidByUserId, String description,
                        BigDecimal amount,
                        Map<Long, BigDecimal> userPercentageMap, Expense.ExpenseCategory category) {
                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
                User paidBy = userRepository.findById(paidByUserId)
                                .orElseThrow(() -> new IllegalArgumentException("Paid by user not found"));
                // Validate percentages sum to 100 (allowing for rounding error)
                BigDecimal totalPercent = userPercentageMap.values().stream()
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (totalPercent.subtract(new BigDecimal("100")).abs().compareTo(new BigDecimal("0.01")) > 0) {
                        throw new IllegalArgumentException("Split percentages must sum to 100");
                }
                // Validate all users are in the group
                for (Long userId : userPercentageMap.keySet()) {
                        User user = userRepository.findById(userId)
                                        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
                        if (!group.getMembers().contains(user)) {
                                throw new IllegalArgumentException("User is not a member of the group");
                        }
                }
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
                // Create splits
                BigDecimal totalAssigned = BigDecimal.ZERO;
                int i = 0;
                int n = userPercentageMap.size();
                for (Map.Entry<Long, BigDecimal> entry : userPercentageMap.entrySet()) {
                        User user = userRepository.findById(entry.getKey()).get();
                        BigDecimal percent = entry.getValue();
                        BigDecimal shareAmount = amount.multiply(percent).divide(new BigDecimal("100"), 2,
                                        RoundingMode.HALF_UP);
                        if (++i == n) {
                                // Assign remainder to last user to avoid rounding issues
                                shareAmount = amount.subtract(totalAssigned);
                        }
                        ExpenseSplit split = ExpenseSplit.builder()
                                        .expense(expense)
                                        .user(user)
                                        .shareAmount(shareAmount)
                                        .splitType(ExpenseSplit.SplitType.PERCENTAGE)
                                        .settled(false)
                                        .build();
                        expense.addSplit(split);
                        totalAssigned = totalAssigned.add(shareAmount);
                }
                expenseSplitRepository.saveAll(expense.getSplits());
                return expense;
        }

        @Override
        public Expense createExpenseWithShareSplits(Long groupId, Long paidByUserId, String description,
                        BigDecimal amount,
                        Map<Long, Integer> userShareMap, Expense.ExpenseCategory category) {
                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
                User paidBy = userRepository.findById(paidByUserId)
                                .orElseThrow(() -> new IllegalArgumentException("Paid by user not found"));
                int totalShares = userShareMap.values().stream().mapToInt(Integer::intValue).sum();
                if (totalShares <= 0) {
                        throw new IllegalArgumentException("Total shares must be greater than zero");
                }
                // Validate all users are in the group
                for (Long userId : userShareMap.keySet()) {
                        User user = userRepository.findById(userId)
                                        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
                        if (!group.getMembers().contains(user)) {
                                throw new IllegalArgumentException("User is not a member of the group");
                        }
                }
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
                // Create splits
                BigDecimal totalAssigned = BigDecimal.ZERO;
                int i = 0;
                int n = userShareMap.size();
                for (Map.Entry<Long, Integer> entry : userShareMap.entrySet()) {
                        User user = userRepository.findById(entry.getKey()).get();
                        int shares = entry.getValue();
                        BigDecimal shareAmount = amount.multiply(BigDecimal.valueOf(shares))
                                        .divide(BigDecimal.valueOf(totalShares), 2, RoundingMode.HALF_UP);
                        if (++i == n) {
                                // Assign remainder to last user to avoid rounding issues
                                shareAmount = amount.subtract(totalAssigned);
                        }
                        ExpenseSplit split = ExpenseSplit.builder()
                                        .expense(expense)
                                        .user(user)
                                        .shareAmount(shareAmount)
                                        .splitType(ExpenseSplit.SplitType.SHARES)
                                        .settled(false)
                                        .build();
                        expense.addSplit(split);
                        totalAssigned = totalAssigned.add(shareAmount);
                }
                expenseSplitRepository.saveAll(expense.getSplits());
                return expense;
        }
}
