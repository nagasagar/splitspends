package com.dasa.splitspends.security;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.GroupRepository;
import com.dasa.splitspends.repository.UserRepository;

/**
 * Central authorization service that defines access control rules for the
 * SplitSpends application.
 * 
 * Authorization Rules Summary:
 * 
 * USER DATA ACCESS:
 * - Users can only see their own profile details
 * - Users can see basic info (name, email) of other users who are in their
 * groups
 * - Users cannot see other users' private settings, balances, or expenses
 * 
 * GROUP DATA ACCESS:
 * - Users can only see groups they are members of
 * - Public group basic info can be seen by anyone for search purposes
 * - Only group members can see: member list, expenses, balances, detailed
 * settings
 * - Only group admins can see: admin list, invitation settings, advanced
 * statistics
 * 
 * EXPENSE DATA ACCESS:
 * - Users can only see expenses from groups they are members of
 * - Users can see all details of expenses they are involved in (paid or split)
 * - Users can see basic details of other group expenses (amount, description,
 * date)
 * 
 * MODIFICATION PERMISSIONS:
 * - Users can only modify their own profile
 * - Only group admins can modify group settings
 * - Only group admins can add/remove members
 * - Users can modify expenses they created (within reasonable time limits)
 * - Group admins can modify any group expense
 */
@Service
public class AuthorizationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    // ========== CURRENT USER UTILITIES ==========

    /**
     * Get the currently authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No authenticated user found");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("Authenticated user not found in database"));
    }

    /**
     * Get current user ID
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Check if current user matches the given user ID
     */
    public boolean isCurrentUser(Long userId) {
        return getCurrentUserId().equals(userId);
    }

    // ========== SUPER ADMIN ACCESS CONTROL ==========

    /**
     * Check if current user is a super admin
     */
    public boolean isSuperAdmin() {
        try {
            return getCurrentUser().isSuperAdmin();
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Check if current user has admin privileges
     */
    public boolean hasAdminPrivileges() {
        try {
            return getCurrentUser().hasAdminPrivileges();
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Check if current user has system access (support/admin)
     */
    public boolean hasSystemAccess() {
        try {
            return getCurrentUser().hasSystemAccess();
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Check if current user can provide support
     */
    public boolean canProvideSupport() {
        try {
            return getCurrentUser().canProvideSupport();
        } catch (SecurityException e) {
            return false;
        }
    }

    // ========== USER ACCESS CONTROL ==========

    /**
     * Check if current user can view another user's profile
     * Rules: Can view own profile OR basic info of users in shared groups
     */
    public boolean canViewUserProfile(Long targetUserId) {
        if (isCurrentUser(targetUserId)) {
            return true; // Can always view own profile
        }

        // Can view basic info of users in shared groups
        return hasSharedGroups(getCurrentUserId(), targetUserId);
    }

    /**
     * Check if current user can view detailed user information (balances, settings,
     * etc.)
     * Rules: Only own detailed information
     */
    public boolean canViewUserDetails(Long targetUserId) {
        return isCurrentUser(targetUserId);
    }

    /**
     * Check if current user can modify another user's profile
     * Rules: Only own profile
     */
    public boolean canModifyUser(Long targetUserId) {
        return isCurrentUser(targetUserId);
    }

    /**
     * Check if current user can view another user's groups
     * Rules: Only own groups
     */
    public boolean canViewUserGroups(Long targetUserId) {
        return isCurrentUser(targetUserId);
    }

    /**
     * Check if current user can view another user's expenses
     * Rules: Only own expenses
     */
    public boolean canViewUserExpenses(Long targetUserId) {
        return isCurrentUser(targetUserId);
    }

    // ========== GROUP ACCESS CONTROL ==========

    /**
     * Check if current user can view group information
     * Rules: Group members OR public groups (basic info only)
     */
    public boolean canViewGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Group members can always view
        if (isGroupMember(groupId, getCurrentUserId())) {
            return true;
        }

        // Public groups can be viewed for basic info
        return group.getPrivacyLevel() == Group.PrivacyLevel.PUBLIC;
    }

    /**
     * Check if current user can view detailed group information (members, balances,
     * etc.)
     * Rules: Only group members
     */
    public boolean canViewGroupDetails(Long groupId) {
        return isGroupMember(groupId, getCurrentUserId());
    }

    /**
     * Check if current user can view group members
     * Rules: Only group members can see member list
     */
    public boolean canViewGroupMembers(Long groupId) {
        return isGroupMember(groupId, getCurrentUserId());
    }

    /**
     * Check if current user can view group admins
     * Rules: Only group members can see admin list
     */
    public boolean canViewGroupAdmins(Long groupId) {
        return isGroupMember(groupId, getCurrentUserId());
    }

    /**
     * Check if current user can modify group
     * Rules: Only group admins
     */
    public boolean canModifyGroup(Long groupId) {
        return isGroupAdmin(groupId, getCurrentUserId());
    }

    /**
     * Check if current user can add members to group
     * Rules: Group admins OR members (depending on invitation policy)
     */
    public boolean canAddGroupMembers(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Admins can always add members
        if (isGroupAdmin(groupId, getCurrentUserId())) {
            return true;
        }

        // Check invitation policy for regular members
        return group.getInvitationPolicy() == Group.InvitationPolicy.ALL_MEMBERS
                && isGroupMember(groupId, getCurrentUserId());
    }

    /**
     * Check if current user can remove group member
     * Rules: Group admins OR removing self
     */
    public boolean canRemoveGroupMember(Long groupId, Long memberUserId) {
        return isGroupAdmin(groupId, getCurrentUserId()) || isCurrentUser(memberUserId);
    }

    /**
     * Check if current user can promote/demote group members
     * Rules: Only group admins
     */
    public boolean canManageGroupRoles(Long groupId) {
        return isGroupAdmin(groupId, getCurrentUserId());
    }

    /**
     * Check if current user can delete group
     * Rules: Only group creator or remaining admin when creator leaves
     */
    public boolean canDeleteGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        User currentUser = getCurrentUser();

        // Group creator can always delete
        if (group.getCreatedBy().getId().equals(currentUser.getId())) {
            return true;
        }

        // Admin can delete if creator is no longer member
        return isGroupAdmin(groupId, getCurrentUserId())
                && !group.getMembers().contains(group.getCreatedBy());
    }

    // ========== EXPENSE ACCESS CONTROL ==========

    /**
     * Check if current user can view group expenses
     * Rules: Only group members
     */
    public boolean canViewGroupExpenses(Long groupId) {
        return isGroupMember(groupId, getCurrentUserId());
    }

    /**
     * Check if current user can view specific expense details
     * Rules: Group members can see all group expenses
     */
    public boolean canViewExpense(Expense expense) {
        return isGroupMember(expense.getGroup().getId(), getCurrentUserId());
    }

    /**
     * Check if current user can create expense in group
     * Rules: Only group members
     */
    public boolean canCreateExpense(Long groupId) {
        return isGroupMember(groupId, getCurrentUserId());
    }

    /**
     * Check if current user can modify expense
     * Rules: Expense creator OR group admin (within time limits)
     */
    public boolean canModifyExpense(Expense expense) {
        User currentUser = getCurrentUser();

        // Expense creator can modify
        if (expense.getPaidBy().getId().equals(currentUser.getId())) {
            return true;
        }

        // Group admin can modify
        return isGroupAdmin(expense.getGroup().getId(), currentUser.getId());
    }

    /**
     * Check if current user can delete expense
     * Rules: Expense creator OR group admin
     */
    public boolean canDeleteExpense(Expense expense) {
        return canModifyExpense(expense); // Same rules as modify
    }

    // ========== STATISTICS AND REPORTS ==========

    /**
     * Check if current user can view group statistics
     * Rules: Group members (basic stats) OR group admins (detailed stats)
     */
    public boolean canViewGroupStats(Long groupId) {
        return isGroupMember(groupId, getCurrentUserId());
    }

    /**
     * Check if current user can view detailed group analytics
     * Rules: Only group admins
     */
    public boolean canViewGroupAnalytics(Long groupId) {
        return isGroupAdmin(groupId, getCurrentUserId());
    }

    /**
     * Check if current user can view group balances
     * Rules: Only group members
     */
    public boolean canViewGroupBalances(Long groupId) {
        return isGroupMember(groupId, getCurrentUserId());
    }

    // ========== NOTIFICATION AND ACTIVITY ==========

    /**
     * Check if current user can view activity logs for group
     * Rules: Group members (limited) OR group admins (full access)
     */
    public boolean canViewGroupActivity(Long groupId) {
        return isGroupMember(groupId, getCurrentUserId());
    }

    // ========== HELPER METHODS ==========

    /**
     * Check if user is member of group
     */
    public boolean isGroupMember(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group == null)
            return false;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return false;

        return group.getMembers().contains(user);
    }

    /**
     * Check if user is admin of group
     */
    public boolean isGroupAdmin(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group == null)
            return false;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return false;

        return group.getAdmins().contains(user);
    }

    /**
     * Check if two users have shared groups
     */
    public boolean hasSharedGroups(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1).orElse(null);
        User user2 = userRepository.findById(userId2).orElse(null);

        if (user1 == null || user2 == null)
            return false;

        Set<Group> user1Groups = user1.getGroups();
        Set<Group> user2Groups = user2.getGroups();

        return user1Groups.stream().anyMatch(user2Groups::contains);
    }

    /**
     * Get list of groups where both users are members
     */
    public List<Group> getSharedGroups(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1).orElse(null);
        User user2 = userRepository.findById(userId2).orElse(null);

        if (user1 == null || user2 == null)
            return List.of();

        return user1.getGroups().stream()
                .filter(group -> user2.getGroups().contains(group))
                .toList();
    }

    /**
     * Validate that current user has permission for operation
     * Throws SecurityException if not authorized
     */
    public void requirePermission(boolean hasPermission, String operation) {
        if (!hasPermission) {
            throw new SecurityException("Access denied: " + operation);
        }
    }

    /**
     * Validate group membership
     */
    public void requireGroupMembership(Long groupId) {
        requirePermission(isGroupMember(groupId, getCurrentUserId()),
                "Group membership required");
    }

    /**
     * Validate group admin rights
     */
    public void requireGroupAdmin(Long groupId) {
        requirePermission(isGroupAdmin(groupId, getCurrentUserId()),
                "Group admin rights required");
    }

    /**
     * Validate user access (self or shared group member)
     */
    public void requireUserAccess(Long targetUserId) {
        requirePermission(canViewUserProfile(targetUserId),
                "User access denied");
    }
}
