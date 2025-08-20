
package com.dasa.splitspends.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_group", columnList = "group_id"),
        @Index(name = "idx_expense_paid_by", columnList = "paid_by"),
        @Index(name = "idx_expense_date", columnList = "date"),
        @Index(name = "idx_expense_status", columnList = "status"),
        @Index(name = "idx_expense_category", columnList = "category"),
        @Index(name = "idx_expense_group_date", columnList = "group_id, date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnore
    private Group group;

    @NotBlank(message = "Description is required")
    @Size(min = 1, max = 500, message = "Description must be between 1 and 500 characters")
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by", nullable = false)
    @JsonIgnore
    private User paidBy;

    // Receipt/attachment support
    @Column(name = "attachment_url", length = 1000)
    private String attachmentUrl;

    @Column(name = "attachment_filename", length = 255)
    private String attachmentFilename;

    // Expense categorization
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    @Builder.Default
    private ExpenseCategory category = ExpenseCategory.OTHER;

    // Currency support (ISO 4217 codes)
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    // Expense status tracking
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ExpenseStatus status = ExpenseStatus.CONFIRMED;

    // Optional notes/memo
    @Column(name = "notes", length = 1000)
    private String notes;

    // Splits relationship
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private Set<ExpenseSplit> splits = new HashSet<>();

    // Attachments relationship (receipts, invoices, etc.)
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private Set<Attachment> attachments = new HashSet<>();

    // ========== AUDIT FIELDS ==========

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    @JsonIgnore
    private User updatedBy;

    // ========== BUSINESS LOGIC ==========

    @PrePersist
    private void prePersist() {
        if (date == null) {
            date = LocalDateTime.now();
        }
        if (status == null) {
            status = ExpenseStatus.CONFIRMED;
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Add a split to this expense
     */
    public void addSplit(ExpenseSplit split) {
        splits.add(split);
        split.setExpense(this);
    }

    /**
     * Remove a split from this expense
     */
    public void removeSplit(ExpenseSplit split) {
        splits.remove(split);
        split.setExpense(null);
    }

    /**
     * Add an attachment to this expense
     */
    public void addAttachment(Attachment attachment) {
        if (attachments == null) {
            attachments = new HashSet<>();
        }
        attachments.add(attachment);
        attachment.setExpense(this);
    }

    /**
     * Remove an attachment from this expense
     */
    public void removeAttachment(Attachment attachment) {
        if (attachments != null) {
            attachments.remove(attachment);
            attachment.setExpense(null);
        }
    }

    /**
     * Get active (non-deleted) attachments
     */
    public Set<Attachment> getActiveAttachments() {
        if (attachments == null)
            return new HashSet<>();
        return attachments.stream()
                .filter(attachment -> !attachment.isDeleted())
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Check if expense has receipt attachments
     */
    public boolean hasReceipts() {
        return getActiveAttachments().stream()
                .anyMatch(attachment -> attachment.getAttachmentType() == Attachment.AttachmentType.RECEIPT_IMAGE);
    }

    /**
     * Get total amount of all splits
     */
    public BigDecimal getTotalSplitAmount() {
        return splits.stream()
                .map(ExpenseSplit::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Check if splits equal the expense amount
     */
    public boolean isSplitAmountValid() {
        BigDecimal totalSplits = getTotalSplitAmount();
        // Allow 1 cent difference for rounding
        return amount.subtract(totalSplits).abs()
                .compareTo(new BigDecimal("0.01")) <= 0;
    }

    /**
     * Get number of people involved in this expense
     */
    public int getParticipantCount() {
        return splits.size();
    }

    /**
     * Check if all splits are settled
     */
    public boolean isFullySettled() {
        return splits.stream().allMatch(ExpenseSplit::isSettled);
    }

    /**
     * Get unsettled amount for this expense
     */
    public BigDecimal getUnsettledAmount() {
        return splits.stream()
                .filter(split -> !split.isSettled())
                .map(ExpenseSplit::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Check if this expense involves a specific user
     */
    public boolean involvesUser(User user) {
        if (user == null)
            return false;
        return paidBy.equals(user) ||
                splits.stream().anyMatch(split -> user.equals(split.getUser()));
    }

    /**
     * Get formatted amount with currency
     */
    public String getFormattedAmount() {
        return String.format("%s %.2f", currency, amount);
    }

    /**
     * Get expense age in days
     */
    public long getAgeInDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(date, LocalDateTime.now());
    }

    // ========== ENUMS ==========

    public enum ExpenseCategory {
        FOOD_DRINKS("Food & Drinks"),
        TRANSPORTATION("Transportation"),
        ACCOMMODATION("Accommodation"),
        ENTERTAINMENT("Entertainment"),
        SHOPPING("Shopping"),
        UTILITIES("Utilities"),
        HEALTHCARE("Healthcare"),
        EDUCATION("Education"),
        TRAVEL("Travel"),
        GROCERIES("Groceries"),
        RESTAURANTS("Restaurants"),
        GAS("Gas/Fuel"),
        PARKING("Parking"),
        OTHER("Other");

        private final String displayName;

        ExpenseCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ExpenseStatus {
        DRAFT("Draft - not yet confirmed"),
        CONFIRMED("Confirmed and active"),
        CANCELLED("Cancelled"),
        DELETED("Soft deleted");

        private final String description;

        ExpenseStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
