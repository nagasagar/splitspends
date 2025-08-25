package com.dasa.splitspends.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Authorization annotations for SplitSpends application.
 * These annotations provide declarative security for common authorization
 * patterns.
 */
public class AuthorizeAccess {

    /**
     * Ensures the current user can access their own data
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.isCurrentUser(#userId)")
    public @interface SelfOnly {
    }

    /**
     * Ensures the current user is a member of the specified group
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.canViewGroupDetails(#groupId)")
    public @interface GroupMember {
    }

    /**
     * Ensures the current user is an admin of the specified group
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.canModifyGroup(#groupId)")
    public @interface GroupAdmin {
    }

    /**
     * Ensures the current user can view the specified group (member or public)
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.canViewGroup(#groupId)")
    public @interface CanViewGroup {
    }

    /**
     * Ensures the current user can view the target user's profile
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.canViewUserProfile(#userId)")
    public @interface CanViewUser {
    }

    /**
     * Ensures the current user can modify the target user's data
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.canModifyUser(#userId)")
    public @interface CanModifyUser {
    }

    /**
     * Ensures the current user can view group expenses
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.canViewGroupExpenses(#groupId)")
    public @interface CanViewGroupExpenses {
    }

    /**
     * Ensures the current user can create expenses in the group
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.canCreateExpense(#groupId)")
    public @interface CanCreateExpense {
    }

    /**
     * Ensures the current user can add members to the group
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.canAddGroupMembers(#groupId)")
    public @interface CanAddMembers {
    }

    /**
     * Ensures the current user can remove the specified member from the group
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.canRemoveGroupMember(#groupId, #memberUserId)")
    public @interface CanRemoveMember {
    }

    /**
     * Ensures the current user can manage group roles (promote/demote)
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.canManageGroupRoles(#groupId)")
    public @interface CanManageRoles {
    }

    /**
     * Ensures the current user can view group statistics
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.canViewGroupStats(#groupId)")
    public @interface CanViewGroupStats {
    }

    /**
     * Ensures the current user can view detailed group analytics
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.canViewGroupAnalytics(#groupId)")
    public @interface CanViewGroupAnalytics {
    }

    /**
     * Ensures the current user can delete the group
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.canDeleteGroup(#groupId)")
    public @interface CanDeleteGroup {
    }

    /**
     * Combined annotation for common user data access patterns
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.canViewUserProfile(#userId)")
    public @interface UserDataAccess {
    }

    /**
     * Combined annotation for group management operations
     */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@authorizationService.canModifyGroup(#groupId)")
    public @interface GroupManagement {
    }
}
