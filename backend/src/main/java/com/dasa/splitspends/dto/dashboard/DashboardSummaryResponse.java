package com.dasa.splitspends.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

import com.dasa.splitspends.dto.activity.ActivityLogResponse;
import com.dasa.splitspends.dto.expense.ExpenseResponse;
import com.dasa.splitspends.dto.group.GroupResponse;
import com.dasa.splitspends.dto.settlement.SettlementResponse;

public class DashboardSummaryResponse {
    private UserSummary userSummary;
    private List<GroupResponse> recentGroups;
    private List<ExpenseResponse> recentExpenses;
    private List<SettlementResponse> pendingSettlements;
    private List<ActivityLogResponse> recentActivities;
    private FinancialSummary financialSummary;

    public static class UserSummary {
        private String name;
        private String email;
        private String profilePictureUrl;
        private int totalGroups;
        private int totalExpenses;
        private int pendingSettlementsCount;

        // Constructors
        public UserSummary() {
        }

        public UserSummary(String name, String email, String profilePictureUrl,
                int totalGroups, int totalExpenses, int pendingSettlementsCount) {
            this.name = name;
            this.email = email;
            this.profilePictureUrl = profilePictureUrl;
            this.totalGroups = totalGroups;
            this.totalExpenses = totalExpenses;
            this.pendingSettlementsCount = pendingSettlementsCount;
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getProfilePictureUrl() {
            return profilePictureUrl;
        }

        public void setProfilePictureUrl(String profilePictureUrl) {
            this.profilePictureUrl = profilePictureUrl;
        }

        public int getTotalGroups() {
            return totalGroups;
        }

        public void setTotalGroups(int totalGroups) {
            this.totalGroups = totalGroups;
        }

        public int getTotalExpenses() {
            return totalExpenses;
        }

        public void setTotalExpenses(int totalExpenses) {
            this.totalExpenses = totalExpenses;
        }

        public int getPendingSettlementsCount() {
            return pendingSettlementsCount;
        }

        public void setPendingSettlementsCount(int pendingSettlementsCount) {
            this.pendingSettlementsCount = pendingSettlementsCount;
        }
    }

    public static class FinancialSummary {
        private BigDecimal totalOwed;
        private BigDecimal totalOwing;
        private BigDecimal netBalance;
        private BigDecimal totalPaid;
        private BigDecimal totalReceived;
        private BigDecimal pendingSettlementAmount;

        // Constructors
        public FinancialSummary() {
        }

        public FinancialSummary(BigDecimal totalOwed, BigDecimal totalOwing, BigDecimal netBalance,
                BigDecimal totalPaid, BigDecimal totalReceived, BigDecimal pendingSettlementAmount) {
            this.totalOwed = totalOwed;
            this.totalOwing = totalOwing;
            this.netBalance = netBalance;
            this.totalPaid = totalPaid;
            this.totalReceived = totalReceived;
            this.pendingSettlementAmount = pendingSettlementAmount;
        }

        // Getters and Setters
        public BigDecimal getTotalOwed() {
            return totalOwed;
        }

        public void setTotalOwed(BigDecimal totalOwed) {
            this.totalOwed = totalOwed;
        }

        public BigDecimal getTotalOwing() {
            return totalOwing;
        }

        public void setTotalOwing(BigDecimal totalOwing) {
            this.totalOwing = totalOwing;
        }

        public BigDecimal getNetBalance() {
            return netBalance;
        }

        public void setNetBalance(BigDecimal netBalance) {
            this.netBalance = netBalance;
        }

        public BigDecimal getTotalPaid() {
            return totalPaid;
        }

        public void setTotalPaid(BigDecimal totalPaid) {
            this.totalPaid = totalPaid;
        }

        public BigDecimal getTotalReceived() {
            return totalReceived;
        }

        public void setTotalReceived(BigDecimal totalReceived) {
            this.totalReceived = totalReceived;
        }

        public BigDecimal getPendingSettlementAmount() {
            return pendingSettlementAmount;
        }

        public void setPendingSettlementAmount(BigDecimal pendingSettlementAmount) {
            this.pendingSettlementAmount = pendingSettlementAmount;
        }
    }

    // Constructors
    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(UserSummary userSummary, List<GroupResponse> recentGroups,
            List<ExpenseResponse> recentExpenses, List<SettlementResponse> pendingSettlements,
            List<ActivityLogResponse> recentActivities, FinancialSummary financialSummary) {
        this.userSummary = userSummary;
        this.recentGroups = recentGroups;
        this.recentExpenses = recentExpenses;
        this.pendingSettlements = pendingSettlements;
        this.recentActivities = recentActivities;
        this.financialSummary = financialSummary;
    }

    // Getters and Setters
    public UserSummary getUserSummary() {
        return userSummary;
    }

    public void setUserSummary(UserSummary userSummary) {
        this.userSummary = userSummary;
    }

    public List<GroupResponse> getRecentGroups() {
        return recentGroups;
    }

    public void setRecentGroups(List<GroupResponse> recentGroups) {
        this.recentGroups = recentGroups;
    }

    public List<ExpenseResponse> getRecentExpenses() {
        return recentExpenses;
    }

    public void setRecentExpenses(List<ExpenseResponse> recentExpenses) {
        this.recentExpenses = recentExpenses;
    }

    public List<SettlementResponse> getPendingSettlements() {
        return pendingSettlements;
    }

    public void setPendingSettlements(List<SettlementResponse> pendingSettlements) {
        this.pendingSettlements = pendingSettlements;
    }

    public List<ActivityLogResponse> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<ActivityLogResponse> recentActivities) {
        this.recentActivities = recentActivities;
    }

    public FinancialSummary getFinancialSummary() {
        return financialSummary;
    }

    public void setFinancialSummary(FinancialSummary financialSummary) {
        this.financialSummary = financialSummary;
    }
}
