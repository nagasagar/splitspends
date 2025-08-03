package com.dasa.splitspends.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "settle_ups", indexes = {
        @Index(name = "idx_settle_up_payer", columnList = "payer_id"),
        @Index(name = "idx_settle_up_payee", columnList = "payee_id"),
        @Index(name = "idx_settle_up_group", columnList = "group_id"),
        @Index(name = "idx_settle_up_status", columnList = "status"),
        @Index(name = "idx_settle_up_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettleUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== SETTLEMENT INFORMATION ==========

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer; // Person who will pay

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id", nullable = false)
    private User payee; // Person who will receive payment

    @NotNull
    @DecimalMin(value = "0.01", message = "Settlement amount must be positive")
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    // ========== SETTLEMENT STATUS ==========

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SettlementStatus status = SettlementStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;

    // ========== SETTLEMENT TRACKING ==========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by")
    private User initiatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by")
    private User confirmedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by")
    private User rejectedBy;

    @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // Reference to external payment (e.g., bank transaction ID)
    @Column(name = "external_transaction_id", length = 100)
    private String externalTransactionId;

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Confirm the settlement
     */
    public void confirm(User confirmedByUser) {
        this.status = SettlementStatus.COMPLETED;
        this.confirmedAt = LocalDateTime.now();
        this.confirmedBy = confirmedByUser;
    }

    /**
     * Reject the settlement
     */
    public void reject(User rejectedByUser, String reason) {
        this.status = SettlementStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
        this.rejectedBy = rejectedByUser;
        this.rejectionReason = reason;
    }

    /**
     * Mark settlement as in progress
     */
    public void markInProgress() {
        this.status = SettlementStatus.IN_PROGRESS;
    }

    /**
     * Cancel the settlement
     */
    public void cancel() {
        this.status = SettlementStatus.CANCELLED;
    }

    /**
     * Check if settlement is completed
     */
    public boolean isCompleted() {
        return SettlementStatus.COMPLETED.equals(status);
    }

    /**
     * Check if settlement is pending
     */
    public boolean isPending() {
        return SettlementStatus.PENDING.equals(status);
    }

    /**
     * Get formatted amount with currency
     */
    public String getFormattedAmount() {
        return String.format("%s %.2f", currency, amount);
    }

    /**
     * Get settlement description
     */
    public String getDescription() {
        return String.format("%s pays %s %s",
                payer.getName(),
                payee.getName(),
                getFormattedAmount());
    }

    // ========== VALIDATION METHODS ==========

    @PrePersist
    private void validateSettlement() {
        // Ensure payer and payee are different
        if (payer != null && payee != null && payer.equals(payee)) {
            throw new IllegalStateException("Payer and payee cannot be the same person");
        }

        // Ensure both users are members of the group
        if (group != null) {
            if (payer != null && !group.isMember(payer)) {
                throw new IllegalStateException("Payer must be a member of the group");
            }
            if (payee != null && !group.isMember(payee)) {
                throw new IllegalStateException("Payee must be a member of the group");
            }
        }

        // Set default currency from group if not specified
        if (currency == null && group != null) {
            currency = group.getDefaultCurrency();
        }
    }

    // ========== ENUMS ==========

    public enum SettlementStatus {
        PENDING("Pending confirmation"),
        IN_PROGRESS("Payment in progress"),
        COMPLETED("Settlement completed"),
        REJECTED("Settlement rejected"),
        CANCELLED("Settlement cancelled");

        private final String description;

        SettlementStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum PaymentMethod {
        CASH("Cash payment"),
        UPI("UPI");

        private final String description;

        PaymentMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
