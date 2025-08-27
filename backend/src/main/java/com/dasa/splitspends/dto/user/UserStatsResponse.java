package com.dasa.splitspends.dto.user;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {

    // Basic user information
    private Long userId;
    private String userName;
    private String userEmail;

    // Group statistics
    private Integer totalGroups;
    private Integer activeGroups;

    // Expense statistics
    private Long totalExpenses;
    private BigDecimal totalExpenseAmount;
    private Long expensesThisMonth;
    private BigDecimal expenseAmountThisMonth;

    // Financial balance information
    private BigDecimal totalOwed; // Amount others owe to this user
    private BigDecimal totalOwing; // Amount this user owes to others
    private BigDecimal netBalance; // totalOwed - totalOwing (positive = user is owed money)
    private BigDecimal totalPaid; // Total amount this user has paid

    // Settlement statistics
    private Integer pendingSettlements;
    private BigDecimal pendingSettlementAmount;
    private Integer completedSettlements;
    private BigDecimal completedSettlementAmount;

    // Activity statistics
    private Long recentActivityCount; // Activities in last 30 days
    private String lastActivityDate;

    // Financial health indicators
    private String balanceStatus; // "POSITIVE", "NEGATIVE", "BALANCED"
    private Boolean hasOutstandingDebts;
    private Boolean hasUnclaimedCredits;

    /**
     * Calculate balance status based on net balance
     */
    public String getBalanceStatus() {
        if (netBalance == null)
            return "UNKNOWN";

        if (netBalance.compareTo(BigDecimal.ZERO) > 0) {
            return "POSITIVE"; // User is owed money
        } else if (netBalance.compareTo(BigDecimal.ZERO) < 0) {
            return "NEGATIVE"; // User owes money
        } else {
            return "BALANCED"; // User is even
        }
    }

    /**
     * Check if user has outstanding debts
     */
    public Boolean getHasOutstandingDebts() {
        return totalOwing != null && totalOwing.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if user has unclaimed credits
     */
    public Boolean getHasUnclaimedCredits() {
        return totalOwed != null && totalOwed.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Get formatted net balance
     */
    public String getFormattedNetBalance() {
        if (netBalance == null)
            return "$0.00";
        return String.format("$%.2f", netBalance);
    }

    /**
     * Get average expense amount
     */
    public BigDecimal getAverageExpenseAmount() {
        if (totalExpenses == null || totalExpenses == 0 || totalExpenseAmount == null) {
            return BigDecimal.ZERO;
        }
        return totalExpenseAmount.divide(BigDecimal.valueOf(totalExpenses), 2, java.math.RoundingMode.HALF_UP);
    }
}
