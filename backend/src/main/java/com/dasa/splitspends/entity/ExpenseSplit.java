package com.dasa.splitspends.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "expense_splits", indexes = {
        @Index(name = "idx_expense_split_user", columnList = "user_id"),
        @Index(name = "idx_expense_split_expense", columnList = "expense_id"),
        @Index(name = "idx_expense_split_settled", columnList = "settled"),
        @Index(name = "idx_expense_split_user_settled", columnList = "user_id, settled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @DecimalMin(value = "0.01", message = "Share amount must be positive")
    @Column(name = "share_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal shareAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_type", length = 20)
    @Builder.Default
    private SplitType splitType = SplitType.EQUAL;

    // For percentage splits (0.00 to 100.00)
    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @Builder.Default
    @Column(name = "settled", nullable = false)
    private boolean settled = false;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    // User who marked this split as settled (for audit trail)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settled_by")
    private User settledBy;

    // Optional note when settling (e.g., "Paid via UPI")
    @Column(name = "settlement_note", length = 500)
    private String settlementNote;

    // ========== AUDIT FIELDS ==========

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========== BUSINESS LOGIC ==========

    @PrePersist
    @PreUpdate
    private void validateSplit() {
        // Ensure settlement timestamp is set when marked as settled
        if (settled && settledAt == null) {
            settledAt = LocalDateTime.now();
        }

        // Clear settlement data when marking as unsettled
        if (!settled) {
            settledAt = null;
            settledBy = null;
            settlementNote = null;
        }

        // Validate percentage for percentage splits
        if (splitType == SplitType.PERCENTAGE && percentage != null) {
            if (percentage.compareTo(BigDecimal.ZERO) < 0 ||
                    percentage.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("Percentage must be between 0 and 100");
            }
        }
    }

    // ========== ENTITY HELPER METHODS (Simple State Operations) ==========

    /**
     * Mark this split as settled (Simple state change)
     */
    public void markAsSettled(User settledByUser, String note) {
        this.settled = true;
        this.settledAt = LocalDateTime.now();
        this.settledBy = settledByUser;
        this.settlementNote = note;
    }

    /**
     * Mark this split as unsettled (Simple state change)
     */
    public void markAsUnsettled() {
        this.settled = false;
        this.settledAt = null;
        this.settledBy = null;
        this.settlementNote = null;
    }

    /**
     * Get the creditor (person who paid) for this split
     * Simple property access - appropriate for entity
     */
    public User getCreditor() {
        return expense != null ? expense.getPaidBy() : null;
    }

    /**
     * Get the debtor (person who owes) for this split
     * Simple property access - appropriate for entity
     */
    public User getDebtor() {
        return this.user;
    }

    /**
     * Check if this split involves the given user as either creditor or debtor
     */
    public boolean involvesUser(User user) {
        return user != null && (user.equals(this.user) || user.equals(getCreditor()));
    }

    /**
     * Get a simple description of this split
     */
    public String getDescription() {
        if (expense == null || user == null) {
            return "Invalid split";
        }
        return String.format("%s owes %s for %s",
                user.getName(),
                getCreditor() != null ? getCreditor().getName() : "Unknown",
                expense.getDescription());
    }

    // ========== SPLIT TYPE ENUM ==========

    public enum SplitType {
        EQUAL("Equal split among all participants"),
        PERCENTAGE("Split based on percentage"),
        EXACT_AMOUNT("Exact amount specified"),
        SHARES("Split based on shares/units");

        private final String description;

        SplitType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
