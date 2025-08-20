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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "groups", indexes = {
        @Index(name = "idx_group_created_by", columnList = "created_by"),
        @Index(name = "idx_group_status", columnList = "status"),
        @Index(name = "idx_group_created_at", columnList = "created_at"),
        @Index(name = "idx_group_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== BASIC GROUP INFORMATION ==========

    @NotBlank(message = "Group name is required")
    @Size(min = 2, max = 100, message = "Group name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500)
    private String description;

    @Column(name = "group_image_url", length = 500)
    private String groupImageUrl;

    // ========== GROUP SETTINGS ==========

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy_level", length = 20)
    @Builder.Default
    private PrivacyLevel privacyLevel = PrivacyLevel.PRIVATE;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
    @Column(name = "default_currency", length = 3)
    @Builder.Default
    private String defaultCurrency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_policy", length = 20)
    @Builder.Default
    private InvitationPolicy invitationPolicy = InvitationPolicy.ADMIN_ONLY;

    @Column(name = "auto_settle_threshold", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal autoSettleThreshold = BigDecimal.ZERO;

    @Column(name = "allow_external_payments", nullable = false)
    @Builder.Default
    private Boolean allowExternalPayments = true;

    // ========== GROUP STATUS & LIFECYCLE ==========

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private GroupStatus status = GroupStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archived_by")
    private User archivedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ========== RELATIONSHIPS ==========

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "group_members", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "user_id"), indexes = {
            @Index(name = "idx_group_members_group", columnList = "group_id"),
            @Index(name = "idx_group_members_user", columnList = "user_id")
    })
    @Builder.Default
    @JsonIgnore
    private Set<User> members = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "group_admins", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    @JsonIgnore
    private Set<User> admins = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private Set<Expense> expenses = new HashSet<>();

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Add a member to the group
     */
    public void addMember(User user) {
        if (members == null) {
            members = new HashSet<>();
        }
        members.add(user);
        user.getGroups().add(this);
    }

    /**
     * Remove a member from the group
     */
    public void removeMember(User user) {
        if (members != null) {
            members.remove(user);
            user.getGroups().remove(this);
        }
        // Also remove from admins if they were admin
        if (admins != null) {
            admins.remove(user);
        }
    }

    /**
     * Add an admin to the group (also adds as member)
     */
    public void addAdmin(User user) {
        addMember(user); // Ensure they're a member first
        if (admins == null) {
            admins = new HashSet<>();
        }
        admins.add(user);
    }

    /**
     * Remove admin privileges (but keep as member)
     */
    public void removeAdmin(User user) {
        if (admins != null) {
            admins.remove(user);
        }
    }

    /**
     * Check if user is a member of this group
     */
    public boolean isMember(User user) {
        return members != null && members.contains(user);
    }

    /**
     * Check if user is an admin of this group
     */
    public boolean isAdmin(User user) {
        return admins != null && admins.contains(user);
    }

    /**
     * Check if user can invite others to this group
     */
    public boolean canUserInvite(User user) {
        switch (invitationPolicy) {
            case ADMIN_ONLY:
                return isAdmin(user) || user.equals(createdBy);
            case ALL_MEMBERS:
                return isMember(user);
            case CREATOR_ONLY:
                return user.equals(createdBy);
            default:
                return false;
        }
    }

    /**
     * Get member count
     */
    public int getMemberCount() {
        return members != null ? members.size() : 0;
    }

    /**
     * Check if group is active
     */
    public boolean isActive() {
        return GroupStatus.ACTIVE.equals(status) && deletedAt == null;
    }

    /**
     * Check if group is archived
     */
    public boolean isArchived() {
        return GroupStatus.ARCHIVED.equals(status);
    }

    /**
     * Archive the group
     */
    public void archive(User archivedByUser) {
        this.status = GroupStatus.ARCHIVED;
        this.archivedAt = LocalDateTime.now();
        this.archivedBy = archivedByUser;
    }

    /**
     * Reactivate archived group
     */
    public void reactivate() {
        this.status = GroupStatus.ACTIVE;
        this.archivedAt = null;
        this.archivedBy = null;
    }

    /**
     * Soft delete the group
     */
    public void softDelete() {
        this.status = GroupStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Get total expense amount for this group
     */
    public BigDecimal getTotalExpenseAmount() {
        if (expenses == null)
            return BigDecimal.ZERO;
        return expenses.stream()
                .filter(expense -> expense.getStatus() != Expense.ExpenseStatus.DELETED)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get group display name with member count
     */
    public String getDisplayName() {
        return String.format("%s (%d members)", name, getMemberCount());
    }

    // ========== VALIDATION METHODS ==========

    @PrePersist
    @PreUpdate
    private void validateGroup() {
        // Ensure creator is also an admin
        if (createdBy != null) {
            addAdmin(createdBy);
        }

        // Ensure at least one admin exists
        if (admins == null || admins.isEmpty()) {
            throw new IllegalStateException("Group must have at least one admin");
        }

        // Validate currency code
        if (defaultCurrency != null && !defaultCurrency.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Invalid currency code");
        }
    }

    // ========== ENUMS ==========

    public enum PrivacyLevel {
        PUBLIC("Public - anyone can find and join"),
        PRIVATE("Private - invitation only"),
        SECRET("Secret - hidden from search");

        private final String description;

        PrivacyLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum InvitationPolicy {
        CREATOR_ONLY("Only group creator can invite"),
        ADMIN_ONLY("Only admins can invite"),
        ALL_MEMBERS("All members can invite");

        private final String description;

        InvitationPolicy(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum GroupStatus {
        ACTIVE("Active group"),
        ARCHIVED("Archived group"),
        DELETED("Soft deleted group");

        private final String description;

        GroupStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
