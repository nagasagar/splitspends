package com.dasa.splitspends.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.dasa.splitspends.dto.activity.ActivityLogResponse;
import com.dasa.splitspends.dto.dashboard.DashboardSummaryResponse;
import com.dasa.splitspends.dto.expense.ExpenseResponse;
import com.dasa.splitspends.dto.group.GroupResponse;
import com.dasa.splitspends.dto.settlement.SettlementResponse;
import com.dasa.splitspends.entity.ActivityLog;
import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.SettleUp;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.service.UserService.UserStats;

/**
 * Dashboard Service providing both aggregated and granular data access.
 * 
 * Supports multiple API patterns:
 * 1. Full aggregated dashboard (getDashboardSummary)
 * 2. Flexible dashboard with selective loading (getFlexibleDashboard)
 * 3. Individual granular methods (getUserStats, getUserGroups, etc.)
 */
@Service
public class DashboardService {

    private final UserService userService;
    private final GroupService groupService;
    private final ExpenseService expenseService;
    private final SettleUpService settleUpService;
    private final ActivityLogService activityLogService;

    public DashboardService(UserService userService, GroupService groupService,
            ExpenseService expenseService, SettleUpService settleUpService,
            ActivityLogService activityLogService) {
        this.userService = userService;
        this.groupService = groupService;
        this.expenseService = expenseService;
        this.settleUpService = settleUpService;
        this.activityLogService = activityLogService;
    }

    /**
     * Original aggregated dashboard method - kept for backward compatibility
     */
    public DashboardSummaryResponse getDashboardSummary(Long userId) {
        // Get user details
        User user = userService.getUserById(userId);

        // Get user statistics
        UserStats userStats = userService.getUserStats(userId);

        // Get user's recent groups (limit 5)
        List<Group> userGroups = groupService.getUserGroups(userId);
        List<GroupResponse> recentGroups = userGroups.stream()
                .limit(5)
                .map(GroupResponse::fromEntity)
                .toList();

        // Get user's recent expenses (limit 5)
        PageRequest expensePageRequest = PageRequest.of(0, 5,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Expense> userExpenses = expenseService.getUserExpensesPaginated(userId, expensePageRequest);
        List<ExpenseResponse> recentExpenses = userExpenses.stream()
                .map(ExpenseResponse::fromEntity)
                .toList();

        // Get pending settlements (limit 5)
        List<SettleUp> pendingSettlements = settleUpService.getPendingSettlements(userId);
        List<SettlementResponse> pendingSettlementsResponse = pendingSettlements.stream()
                .limit(5)
                .map(SettlementResponse::fromEntity)
                .toList();

        // Get recent activities (limit 10)
        PageRequest activityPageRequest = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ActivityLog> recentActivitiesData = activityLogService.getUserActivities(userId, activityPageRequest)
                .getContent();
        List<ActivityLogResponse> recentActivities = recentActivitiesData.stream()
                .map(ActivityLogResponse::fromEntity)
                .toList();

        // Get financial summary
        BigDecimal pendingAmount = settleUpService.getPendingAmountByUser(userId);

        // Create user summary
        DashboardSummaryResponse.UserSummary userSummary = new DashboardSummaryResponse.UserSummary(
                user.getName(),
                user.getEmail(),
                user.getProfilePictureUrl(),
                userGroups.size(),
                userStats.getTotalExpensesPaid().intValue(),
                pendingSettlements.size());

        // Calculate financial statistics for comprehensive summary
        BigDecimal totalOwed = settleUpService.getPendingAmountByUser(userId); // Amount user owes to others
        BigDecimal totalOwing = getPendingAmountOwedToUser(userId); // Amount others owe to user
        BigDecimal netBalance = totalOwing.subtract(totalOwed); // Net balance
        BigDecimal totalPaid = getTotalPaidByUser(userId); // Total amount user has paid
        BigDecimal totalReceived = getTotalReceivedByUser(userId); // Total amount user has received

        // Create financial summary with calculated values
        DashboardSummaryResponse.FinancialSummary financialSummary = new DashboardSummaryResponse.FinancialSummary(
                totalOwed,
                totalOwing,
                netBalance,
                totalPaid,
                totalReceived,
                pendingAmount);

        return new DashboardSummaryResponse(
                userSummary,
                recentGroups,
                recentExpenses,
                pendingSettlementsResponse,
                recentActivities,
                financialSummary);
    }

    /**
     * Flexible dashboard method - allows clients to specify what data they need
     * 
     * @param userId  User ID
     * @param include Comma-separated list of components
     *                (stats,groups,expenses,settlements,activities)
     * @param limit   Limit for list items
     * @return Map containing only requested components
     */
    public Map<String, Object> getFlexibleDashboard(Long userId, String include, int limit) {
        Map<String, Object> response = new HashMap<>();
        List<String> components = Arrays.asList(include.toLowerCase().split(","));

        if (components.contains("stats")) {
            response.put("stats", getUserStats(userId));
        }

        if (components.contains("groups")) {
            response.put("groups", getUserGroups(userId));
        }

        if (components.contains("expenses")) {
            response.put("expenses", getRecentExpenses(userId, limit));
        }

        if (components.contains("settlements")) {
            response.put("settlements", getPendingSettlements(userId));
        }

        if (components.contains("activities")) {
            response.put("activities", getRecentActivities(userId, limit));
        }

        return response;
    }

    /**
     * Individual granular methods for maximum flexibility
     */

    public Map<String, Object> getUserStats(Long userId) {
        User user = userService.getUserById(userId);
        UserStats userStats = userService.getUserStats(userId);
        List<Group> userGroups = groupService.getUserGroups(userId);
        List<SettleUp> pendingSettlements = settleUpService.getPendingSettlements(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("userName", user.getName());
        stats.put("userEmail", user.getEmail());
        stats.put("profilePictureUrl", user.getProfilePictureUrl());
        stats.put("totalGroups", userGroups.size());
        stats.put("totalExpenses", userStats.getTotalExpensesPaid());
        stats.put("pendingSettlements", pendingSettlements.size());
        // Calculate financial statistics
        // Amount user owes to others (user is payer)
        BigDecimal totalOwed = settleUpService.getPendingAmountByUser(userId);
        // Amount others owe to user (user is payee)
        BigDecimal totalOwing = getPendingAmountOwedToUser(userId);
        // Net balance (positive = user is owed, negative = user owes)
        BigDecimal netBalance = totalOwing.subtract(totalOwed);
        // Total amount user has paid in completed settlements
        BigDecimal totalPaid = getTotalPaidByUser(userId);

        stats.put("totalOwed", totalOwed);
        stats.put("totalOwing", totalOwing);
        stats.put("netBalance", netBalance);
        stats.put("totalPaid", totalPaid);

        return stats;
    }

    public Map<String, Object> getUserGroups(Long userId) {
        List<Group> userGroups = groupService.getUserGroups(userId);
        List<GroupResponse> groupResponses = userGroups.stream()
                .map(GroupResponse::fromEntity)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("groups", groupResponses);
        response.put("totalCount", userGroups.size());

        return response;
    }

    public Map<String, Object> getRecentExpenses(Long userId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Expense> userExpenses = expenseService.getUserExpensesPaginated(userId, pageRequest);
        List<ExpenseResponse> expenseResponses = userExpenses.stream()
                .map(ExpenseResponse::fromEntity)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("expenses", expenseResponses);
        response.put("count", expenseResponses.size());

        return response;
    }

    public Map<String, Object> getPendingSettlements(Long userId) {
        List<SettleUp> pendingSettlements = settleUpService.getPendingSettlements(userId);
        List<SettlementResponse> settlementResponses = pendingSettlements.stream()
                .map(SettlementResponse::fromEntity)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("settlements", settlementResponses);
        response.put("totalCount", pendingSettlements.size());
        response.put("totalAmount", settleUpService.getPendingAmountByUser(userId));

        return response;
    }

    public Map<String, Object> getRecentActivities(Long userId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ActivityLog> activities = activityLogService.getUserActivities(userId, pageRequest)
                .getContent();
        List<ActivityLogResponse> activityResponses = activities.stream()
                .map(ActivityLogResponse::fromEntity)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("activities", activityResponses);
        response.put("count", activityResponses.size());

        return response;
    }

    /**
     * Helper method to get pending amount owed to a user (where user is payee)
     */
    private BigDecimal getPendingAmountOwedToUser(Long userId) {
        List<SettleUp> pendingSettlements = settleUpService.getPendingSettlements(userId);

        return pendingSettlements.stream()
                .filter(settlement -> settlement.getPayee().getId().equals(userId))
                .map(SettleUp::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Helper method to get total amount paid by user in completed settlements
     */
    private BigDecimal getTotalPaidByUser(Long userId) {
        return settleUpService.getTotalAmountSettledByUser(userId);
    }

    /**
     * Helper method to get total amount received by user in completed settlements
     */
    private BigDecimal getTotalReceivedByUser(Long userId) {
        return settleUpService.getTotalAmountReceivedByUser(userId);
    }
}
