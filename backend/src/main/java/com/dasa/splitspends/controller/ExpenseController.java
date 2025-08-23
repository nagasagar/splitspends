package com.dasa.splitspends.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dasa.splitspends.dto.expense.ExpenseStats;
import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.service.ExpenseService;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // Get paginated expenses for a group
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Expense>> getGroupExpenses(@PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // For simplicity, not using Pageable here, but you can adapt as needed
        // You may want to return a Page<Expense> instead
        return ResponseEntity.ok(expenseService
                .getGroupExpenses(groupId, org.springframework.data.domain.PageRequest.of(page, size)).getContent());
    }

    // Get all expenses for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Expense>> getUserExpenses(@PathVariable Long userId) {
        return ResponseEntity.ok(expenseService.getUserExpenses(userId));
    }

    // Create expense with equal splits
    @PostMapping("/group/{groupId}/equal-split")
    public ResponseEntity<Expense> createExpenseWithEqualSplits(
            @PathVariable Long groupId,
            @RequestBody com.dasa.splitspends.dto.expense.CreateExpenseRequest request) {
        Expense expense = expenseService.createExpenseWithEqualSplits(
                groupId,
                request.getPaidByUserId(),
                request.getDescription(),
                request.getAmount(),
                request.getParticipantUserIds(),
                com.dasa.splitspends.entity.Expense.ExpenseCategory.valueOf(request.getCategory()));
        return ResponseEntity.ok(expense);
    }

    // Create expense with custom splits (amounts)
    @PostMapping("/group/{groupId}/custom-split")
    public ResponseEntity<Expense> createExpenseWithCustomSplits(
            @PathVariable Long groupId,
            @RequestBody com.dasa.splitspends.dto.expense.CustomSplitRequest request) {
        Expense expense = expenseService.createExpenseWithCustomSplits(
                groupId,
                request.getPaidByUserId(),
                request.getDescription(),
                request.getAmount(),
                request.getUserAmountMap(),
                com.dasa.splitspends.entity.Expense.ExpenseCategory.valueOf(request.getCategory()));
        return ResponseEntity.ok(expense);
    }

    // Create expense with percentage splits
    @PostMapping("/group/{groupId}/percentage-split")
    public ResponseEntity<Expense> createExpenseWithPercentageSplits(
            @PathVariable Long groupId,
            @RequestBody com.dasa.splitspends.dto.expense.PercentageSplitRequest request) {
        // userPercentageMap: userId -> percentage (should sum to 100)
        Expense expense = expenseService.createExpenseWithPercentageSplits(
                groupId,
                request.getPaidByUserId(),
                request.getDescription(),
                request.getAmount(),
                request.getUserPercentageMap(),
                com.dasa.splitspends.entity.Expense.ExpenseCategory.valueOf(request.getCategory()));
        return ResponseEntity.ok(expense);
    }

    // Create expense with share-based splits
    @PostMapping("/group/{groupId}/share-split")
    public ResponseEntity<Expense> createExpenseWithShareSplits(
            @PathVariable Long groupId,
            @RequestBody com.dasa.splitspends.dto.expense.ShareSplitRequest request) {
        // userShareMap: userId -> number of shares (weights)
        Expense expense = expenseService.createExpenseWithShareSplits(
                groupId,
                request.getPaidByUserId(),
                request.getDescription(),
                request.getAmount(),
                request.getUserShareMap(),
                com.dasa.splitspends.entity.Expense.ExpenseCategory.valueOf(request.getCategory()));
        return ResponseEntity.ok(expense);
    }

    // Update an expense
    @PutMapping("/{expenseId}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable Long expenseId,
            @RequestParam String description,
            @RequestParam BigDecimal amount,
            @RequestParam Expense.ExpenseCategory category,
            @RequestParam(required = false) String notes,
            @RequestParam Long updatedByUserId) {
        // You may want to fetch the User entity for updatedByUserId in the service
        Expense expense = expenseService.updateExpense(expenseId, description, amount, category, notes, null);
        return ResponseEntity.ok(expense);
    }

    // Delete an expense
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId, @RequestParam Long deletedByUserId) {
        // You may want to fetch the User entity for deletedByUserId in the service
        expenseService.deleteExpense(expenseId, null);
        return ResponseEntity.noContent().build();
    }

    // Get expense stats for a user
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<ExpenseStats> getUserExpenseStats(@PathVariable Long userId) {
        return ResponseEntity.ok(expenseService.getExpenseStatsForUser(userId));
    }
}
