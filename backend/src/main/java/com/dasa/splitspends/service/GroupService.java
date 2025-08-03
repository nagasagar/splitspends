package com.dasa.splitspends.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.ExpenseRepository;
import com.dasa.splitspends.repository.GroupRepository;
import com.dasa.splitspends.repository.UserRepository;

@Service
@Transactional
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserService userService;

    // ========== GROUP CREATION & MANAGEMENT ==========

    /**
     * Create a new group
     */
    public Group createGroup(String name, String description, Long createdByUserId,
            Group.PrivacyLevel privacyLevel, String defaultCurrency) {
        User creator = userService.getUserById(createdByUserId);

        // Validate group name uniqueness (optional - depends on requirements)
        if (groupRepository.existsByName(name)) {
            throw new IllegalArgumentException("Group name already exists");
        }

        Group group = Group.builder()
                .name(name)
                .description(description)
                .createdBy(creator)
                .privacyLevel(privacyLevel != null ? privacyLevel : Group.PrivacyLevel.PRIVATE)
                .defaultCurrency(defaultCurrency != null ? defaultCurrency : "USD")
                .status(Group.GroupStatus.ACTIVE)
                .build();

        // Add creator as member and admin
        group.addAdmin(creator);

        return groupRepository.save(group);
    }

    /**
     * Update group information
     */
    public Group updateGroup(Long groupId, String name, String description,
            Group.PrivacyLevel privacyLevel, String defaultCurrency,
            Long updatedByUserId) {
        Group group = getGroupById(groupId);
        User updatedBy = userService.getUserById(updatedByUserId);

        // Check if user has permission to update
        if (!group.isAdmin(updatedBy)) {
            throw new IllegalArgumentException("Only admins can update group information");
        }

        if (name != null && !name.trim().isEmpty()) {
            group.setName(name);
        }
        if (description != null) {
            group.setDescription(description);
        }
        if (privacyLevel != null) {
            group.setPrivacyLevel(privacyLevel);
        }
        if (defaultCurrency != null) {
            group.setDefaultCurrency(defaultCurrency);
        }

        return groupRepository.save(group);
    }

    /**
     * Upload group image
     */
    public Group uploadGroupImage(Long groupId, MultipartFile file, Long uploadedByUserId) {
        Group group = getGroupById(groupId);
        User uploadedBy = userService.getUserById(uploadedByUserId);

        if (!group.isAdmin(uploadedBy)) {
            throw new IllegalArgumentException("Only admins can update group image");
        }

        // TODO: Implement file upload to cloud storage (AWS S3, etc.)
        String filename = "group_" + groupId + "_" + file.getOriginalFilename();
        group.setGroupImageUrl("/uploads/groups/" + filename);

        return groupRepository.save(group);
    }

    // ========== MEMBER MANAGEMENT ==========

    /**
     * Add members to group
     */
    public Group addMembers(Long groupId, List<Long> userIds, Long addedByUserId) {
        Group group = getGroupById(groupId);
        User addedBy = userService.getUserById(addedByUserId);

        // Check permission to invite
        if (!group.canUserInvite(addedBy)) {
            throw new IllegalArgumentException("User does not have permission to invite members");
        }

        List<User> usersToAdd = userService.getUsersByIds(userIds);

        for (User user : usersToAdd) {
            if (!group.isMember(user)) {
                group.addMember(user);
            }
        }

        return groupRepository.save(group);
    }

    /**
     * Remove member from group
     */
    public Group removeMember(Long groupId, Long userIdToRemove, Long removedByUserId) {
        Group group = getGroupById(groupId);
        User removedBy = userService.getUserById(removedByUserId);
        User userToRemove = userService.getUserById(userIdToRemove);

        // Check permissions
        if (!group.isAdmin(removedBy) && !removedBy.equals(userToRemove)) {
            throw new IllegalArgumentException("Only admins can remove members, or users can remove themselves");
        }

        // Prevent removing the creator unless there's another admin
        if (userToRemove.equals(group.getCreatedBy()) && group.getAdmins().size() <= 1) {
            throw new IllegalStateException("Cannot remove creator unless there's another admin");
        }

        // Check for unsettled expenses
        if (hasUnsettledExpenses(group, userToRemove)) {
            throw new IllegalStateException("Cannot remove user with unsettled expenses");
        }

        group.removeMember(userToRemove);
        return groupRepository.save(group);
    }

    /**
     * Promote member to admin
     */
    public Group promoteToAdmin(Long groupId, Long userIdToPromote, Long promotedByUserId) {
        Group group = getGroupById(groupId);
        User promotedBy = userService.getUserById(promotedByUserId);
        User userToPromote = userService.getUserById(userIdToPromote);

        // Check permissions
        if (!group.isAdmin(promotedBy)) {
            throw new IllegalArgumentException("Only admins can promote members");
        }

        if (!group.isMember(userToPromote)) {
            throw new IllegalArgumentException("User is not a member of this group");
        }

        group.addAdmin(userToPromote);
        return groupRepository.save(group);
    }

    /**
     * Demote admin to member
     */
    public Group demoteAdmin(Long groupId, Long userIdToDemote, Long demotedByUserId) {
        Group group = getGroupById(groupId);
        User demotedBy = userService.getUserById(demotedByUserId);
        User userToDemote = userService.getUserById(userIdToDemote);

        // Check permissions
        if (!group.isAdmin(demotedBy)) {
            throw new IllegalArgumentException("Only admins can demote other admins");
        }

        // Prevent demoting the creator
        if (userToDemote.equals(group.getCreatedBy())) {
            throw new IllegalArgumentException("Cannot demote group creator");
        }

        // Ensure at least one admin remains
        if (group.getAdmins().size() <= 1) {
            throw new IllegalStateException("Cannot demote the last admin");
        }

        group.removeAdmin(userToDemote);
        return groupRepository.save(group);
    }

    // ========== GROUP RETRIEVAL ==========

    /**
     * Get group by ID
     */
    public Group getGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }

    /**
     * Get user's groups
     */
    public List<Group> getUserGroups(Long userId) {
        User user = userService.getUserById(userId);
        return groupRepository.findByMembersContaining(user);
    }

    /**
     * Get groups created by user
     */
    public List<Group> getGroupsCreatedByUser(Long userId) {
        User user = userService.getUserById(userId);
        return groupRepository.findByCreatedBy(user);
    }

    /**
     * Search groups by name (public groups only)
     */
    public List<Group> searchPublicGroups(String searchQuery) {
        return groupRepository.findByNameContainingIgnoreCase(searchQuery)
                .stream()
                .filter(group -> group.getPrivacyLevel() == Group.PrivacyLevel.PUBLIC)
                .filter(Group::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Get group members
     */
    public Set<User> getGroupMembers(Long groupId) {
        Group group = getGroupById(groupId);
        return group.getMembers();
    }

    /**
     * Get group admins
     */
    public Set<User> getGroupAdmins(Long groupId) {
        Group group = getGroupById(groupId);
        return group.getAdmins();
    }

    // ========== GROUP LIFECYCLE MANAGEMENT ==========

    /**
     * Archive group
     */
    public Group archiveGroup(Long groupId, Long archivedByUserId) {
        Group group = getGroupById(groupId);
        User archivedBy = userService.getUserById(archivedByUserId);

        if (!group.isAdmin(archivedBy)) {
            throw new IllegalArgumentException("Only admins can archive groups");
        }

        group.archive(archivedBy);
        return groupRepository.save(group);
    }

    /**
     * Reactivate archived group
     */
    public Group reactivateGroup(Long groupId, Long reactivatedByUserId) {
        Group group = getGroupById(groupId);
        User reactivatedBy = userService.getUserById(reactivatedByUserId);

        if (!group.isAdmin(reactivatedBy)) {
            throw new IllegalArgumentException("Only admins can reactivate groups");
        }

        group.reactivate();
        return groupRepository.save(group);
    }

    /**
     * Delete group (soft delete)
     */
    public void deleteGroup(Long groupId, Long deletedByUserId) {
        Group group = getGroupById(groupId);
        User deletedBy = userService.getUserById(deletedByUserId);

        if (!group.getCreatedBy().equals(deletedBy)) {
            throw new IllegalArgumentException("Only group creator can delete the group");
        }

        // Check for unsettled expenses
        if (hasUnsettledExpensesInGroup(group)) {
            throw new IllegalStateException("Cannot delete group with unsettled expenses");
        }

        group.softDelete();
        groupRepository.save(group);
    }

    // ========== GROUP SETTINGS MANAGEMENT ==========

    /**
     * Update group settings
     */
    public Group updateGroupSettings(Long groupId, Group.InvitationPolicy invitationPolicy,
            BigDecimal autoSettleThreshold, Boolean allowExternalPayments,
            Long updatedByUserId) {
        Group group = getGroupById(groupId);
        User updatedBy = userService.getUserById(updatedByUserId);

        if (!group.isAdmin(updatedBy)) {
            throw new IllegalArgumentException("Only admins can update group settings");
        }

        if (invitationPolicy != null) {
            group.setInvitationPolicy(invitationPolicy);
        }
        if (autoSettleThreshold != null) {
            group.setAutoSettleThreshold(autoSettleThreshold);
        }
        if (allowExternalPayments != null) {
            group.setAllowExternalPayments(allowExternalPayments);
        }

        return groupRepository.save(group);
    }

    // ========== GROUP STATISTICS & ANALYTICS ==========

    /**
     * Get group statistics
     */
    public GroupStats getGroupStats(Long groupId) {
        Group group = getGroupById(groupId);

        Long totalExpenses = expenseRepository.countByGroup(group);
        BigDecimal totalAmount = expenseRepository.getTotalAmountByGroup(group);
        BigDecimal settledAmount = expenseRepository.getTotalSettledAmountByGroup(group);

        return GroupStats.builder()
                .groupId(groupId)
                .memberCount(group.getMemberCount())
                .adminCount(group.getAdmins().size())
                .totalExpenses(totalExpenses)
                .totalAmount(totalAmount)
                .settledAmount(settledAmount)
                .createdAt(group.getCreatedAt())
                .isActive(group.isActive())
                .build();
    }

    /**
     * Get group balance summary
     */
    public List<UserBalance> getGroupBalances(Long groupId) {
        Group group = getGroupById(groupId);

        return group.getMembers().stream()
                .map(user -> {
                    BigDecimal totalPaid = expenseRepository.getTotalPaidByUserInGroup(group, user);
                    BigDecimal totalOwed = expenseRepository.getTotalOwedByUserInGroup(group, user);
                    BigDecimal netBalance = totalPaid.subtract(totalOwed);

                    return UserBalance.builder()
                            .user(user)
                            .totalPaid(totalPaid)
                            .totalOwed(totalOwed)
                            .netBalance(netBalance)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ========== VALIDATION HELPERS ==========

    /**
     * Check if user is member of group
     */
    public boolean isUserMemberOfGroup(Long userId, Long groupId) {
        return groupRepository.isUserMemberOfGroup(groupId, userId);
    }

    /**
     * Validate user has permission for group action
     */
    public void validateGroupAccess(Long groupId, Long userId) {
        if (!isUserMemberOfGroup(userId, groupId)) {
            throw new IllegalArgumentException("User is not a member of this group");
        }
    }

    /**
     * Check if group has unsettled expenses
     */
    private boolean hasUnsettledExpensesInGroup(Group group) {
        return expenseRepository.findUnsettledExpensesByGroup(group).size() > 0;
    }

    /**
     * Check if specific user has unsettled expenses in group
     */
    private boolean hasUnsettledExpenses(Group group, User user) {
        return expenseRepository.findUnsettledExpensesByUserInGroup(group, user).size() > 0;
    }

    /**
     * Get users suitable for group invitation
     */
    public List<User> getUsersForInvitation(Long groupId, String searchQuery) {
        Group group = getGroupById(groupId);
        return userRepository.findUsersNotInGroup(searchQuery, group);
    }

    // ========== DTO CLASSES ==========

    @lombok.Builder
    @lombok.Data
    public static class GroupStats {
        private Long groupId;
        private Integer memberCount;
        private Integer adminCount;
        private Long totalExpenses;
        private BigDecimal totalAmount;
        private BigDecimal settledAmount;
        private LocalDateTime createdAt;
        private Boolean isActive;

        public BigDecimal getUnsettledAmount() {
            if (totalAmount == null || settledAmount == null)
                return BigDecimal.ZERO;
            return totalAmount.subtract(settledAmount);
        }

        public double getSettlementPercentage() {
            if (totalAmount == null || totalAmount.equals(BigDecimal.ZERO))
                return 0.0;
            return settledAmount.divide(totalAmount, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }
    }

    @lombok.Builder
    @lombok.Data
    public static class UserBalance {
        private User user;
        private BigDecimal totalPaid;
        private BigDecimal totalOwed;
        private BigDecimal netBalance;

        public boolean owesGroup() {
            return netBalance.compareTo(BigDecimal.ZERO) < 0;
        }

        public boolean isOwedByGroup() {
            return netBalance.compareTo(BigDecimal.ZERO) > 0;
        }

        public BigDecimal getAbsoluteBalance() {
            return netBalance.abs();
        }
    }
}
