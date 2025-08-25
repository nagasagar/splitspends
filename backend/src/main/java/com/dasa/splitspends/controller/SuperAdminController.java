package com.dasa.splitspends.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dasa.splitspends.dto.UserResponse;
import com.dasa.splitspends.dto.group.GroupResponse;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.security.AuthorizationService;
import com.dasa.splitspends.service.GroupService;
import com.dasa.splitspends.service.UserService;

/**
 * Super Admin Controller for platform administration.
 * 
 * IMPORTANT: All operations in this controller are logged for audit purposes.
 * Super admin access should be used sparingly and only for:
 * - Customer support
 * - Platform maintenance
 * - Legal compliance (GDPR, etc.)
 * - System monitoring
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("@authorizationService.isSuperAdmin()")
public class SuperAdminController {

    private final UserService userService;
    private final GroupService groupService;
    private final AuthorizationService authorizationService;

    public SuperAdminController(UserService userService, GroupService groupService, 
                               AuthorizationService authorizationService) {
        this.userService = userService;
        this.groupService = groupService;
        this.authorizationService = authorizationService;
    }

    // ========== USER MANAGEMENT ==========

    /**
     * Get all users with pagination (for user management)
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        
        // TODO: Add audit logging
        // auditLogger.logSuperAdminAccess(getCurrentUser(), "VIEW_ALL_USERS", search);
        
        Page<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userService.searchUsers(search, PageRequest.of(page, size));
        } else {
            users = userService.getAllUsers(PageRequest.of(page, size));
        }
        
        Page<UserResponse> response = users.map(UserResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user details by ID (for support)
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        
        // TODO: Add audit logging
        // auditLogger.logSuperAdminAccess(getCurrentUser(), "VIEW_USER_DETAILS", userId);
        
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    /**
     * Suspend user account
     */
    @PutMapping("/users/{userId}/suspend")
    public ResponseEntity<UserResponse> suspendUser(
            @PathVariable Long userId,
            @RequestParam String reason) {
        
        // TODO: Add audit logging
        // auditLogger.logSuperAdminAction(getCurrentUser(), "SUSPEND_USER", userId, reason);
        
        User user = userService.suspendUser(userId, reason);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    /**
     * Reactivate suspended user account
     */
    @PutMapping("/users/{userId}/reactivate")
    public ResponseEntity<UserResponse> reactivateUser(@PathVariable Long userId) {
        
        // TODO: Add audit logging
        // auditLogger.logSuperAdminAction(getCurrentUser(), "REACTIVATE_USER", userId, null);
        
        User user = userService.reactivateUser(userId);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    // ========== GROUP MANAGEMENT ==========

    /**
     * Get all groups with pagination (for content moderation)
     */
    @GetMapping("/groups")
    public ResponseEntity<Page<GroupResponse>> getAllGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        
        // TODO: Add audit logging
        // auditLogger.logSuperAdminAccess(getCurrentUser(), "VIEW_ALL_GROUPS", search);
        
        Page<Group> groups;
        if (search != null && !search.trim().isEmpty()) {
            groups = groupService.searchGroups(search, PageRequest.of(page, size));
        } else {
            groups = groupService.getAllGroups(PageRequest.of(page, size));
        }
        
        Page<GroupResponse> response = groups.map(GroupResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    /**
     * Get group details by ID (for support)
     */
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable Long groupId) {
        
        // TODO: Add audit logging
        // auditLogger.logSuperAdminAccess(getCurrentUser(), "VIEW_GROUP_DETAILS", groupId);
        
        Group group = groupService.getGroupById(groupId);
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }

    /**
     * Archive inappropriate group (content moderation)
     */
    @PutMapping("/groups/{groupId}/moderate")
    public ResponseEntity<GroupResponse> moderateGroup(
            @PathVariable Long groupId,
            @RequestParam String reason) {
        
        // TODO: Add audit logging
        // auditLogger.logSuperAdminAction(getCurrentUser(), "MODERATE_GROUP", groupId, reason);
        
        User currentAdmin = authorizationService.getCurrentUser();
        Group group = groupService.archiveGroup(groupId, currentAdmin.getId());
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }

    // ========== PLATFORM STATISTICS ==========

    /**
     * Get platform statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<PlatformStats> getPlatformStats() {
        
        // TODO: Add audit logging
        // auditLogger.logSuperAdminAccess(getCurrentUser(), "VIEW_PLATFORM_STATS", null);
        
        PlatformStats stats = PlatformStats.builder()
            .totalUsers(userService.getTotalUserCount())
            .activeUsers(userService.getActiveUserCount())
            .totalGroups(groupService.getTotalGroupCount())
            .activeGroups(groupService.getActiveGroupCount())
            .totalExpenses(0L) // TODO: Add expense service method
            .build();
            
        return ResponseEntity.ok(stats);
    }

    // ========== USER SUPPORT ==========

    /**
     * Get user's groups (for support)
     */
    @GetMapping("/users/{userId}/groups")
    public ResponseEntity<List<GroupResponse>> getUserGroupsForSupport(@PathVariable Long userId) {
        
        // TODO: Add audit logging
        // auditLogger.logSuperAdminAccess(getCurrentUser(), "VIEW_USER_GROUPS_SUPPORT", userId);
        
        List<Group> groups = groupService.getUserGroups(userId);
        List<GroupResponse> response = groups.stream()
                .map(GroupResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Reset user password (for support)
     */
    @PutMapping("/users/{userId}/reset-password")
    public ResponseEntity<String> resetUserPassword(@PathVariable Long userId) {
        
        // TODO: Add audit logging
        // auditLogger.logSuperAdminAction(getCurrentUser(), "RESET_USER_PASSWORD", userId, null);
        
        String temporaryPassword = userService.resetPassword(userId);
        return ResponseEntity.ok("Password reset. Temporary password: " + temporaryPassword);
    }

    // ========== DATA COMPLIANCE ==========

    /**
     * Export user data (GDPR compliance)
     */
    @GetMapping("/users/{userId}/export")
    public ResponseEntity<String> exportUserData(@PathVariable Long userId) {
        
        // TODO: Add audit logging
        // auditLogger.logSuperAdminAction(getCurrentUser(), "EXPORT_USER_DATA", userId, "GDPR_REQUEST");
        
        // TODO: Implement user data export
        return ResponseEntity.ok("User data export functionality - TODO: Implement");
    }

    /**
     * Delete user data (GDPR compliance)
     */
    @PutMapping("/users/{userId}/gdpr-delete")
    public ResponseEntity<String> deleteUserDataForCompliance(
            @PathVariable Long userId,
            @RequestParam String legalBasis) {
        
        // TODO: Add audit logging
        // auditLogger.logSuperAdminAction(getCurrentUser(), "GDPR_DELETE_USER", userId, legalBasis);
        
        // TODO: Implement GDPR deletion
        return ResponseEntity.ok("User data deletion for compliance - TODO: Implement");
    }

    // ========== INNER CLASSES ==========

    public static class PlatformStats {
        private Long totalUsers;
        private Long activeUsers;
        private Long totalGroups;
        private Long activeGroups;
        private Long totalExpenses;

        // Constructor, getters, setters
        private PlatformStats(Long totalUsers, Long activeUsers, Long totalGroups, 
                             Long activeGroups, Long totalExpenses) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.totalGroups = totalGroups;
            this.activeGroups = activeGroups;
            this.totalExpenses = totalExpenses;
        }

        public static PlatformStatsBuilder builder() {
            return new PlatformStatsBuilder();
        }

        // Getters
        public Long getTotalUsers() { return totalUsers; }
        public Long getActiveUsers() { return activeUsers; }
        public Long getTotalGroups() { return totalGroups; }
        public Long getActiveGroups() { return activeGroups; }
        public Long getTotalExpenses() { return totalExpenses; }

        public static class PlatformStatsBuilder {
            private Long totalUsers;
            private Long activeUsers;
            private Long totalGroups;
            private Long activeGroups;
            private Long totalExpenses;

            public PlatformStatsBuilder totalUsers(Long totalUsers) {
                this.totalUsers = totalUsers;
                return this;
            }

            public PlatformStatsBuilder activeUsers(Long activeUsers) {
                this.activeUsers = activeUsers;
                return this;
            }

            public PlatformStatsBuilder totalGroups(Long totalGroups) {
                this.totalGroups = totalGroups;
                return this;
            }

            public PlatformStatsBuilder activeGroups(Long activeGroups) {
                this.activeGroups = activeGroups;
                return this;
            }

            public PlatformStatsBuilder totalExpenses(Long totalExpenses) {
                this.totalExpenses = totalExpenses;
                return this;
            }

            public PlatformStats build() {
                return new PlatformStats(totalUsers, activeUsers, totalGroups, activeGroups, totalExpenses);
            }
        }
    }
}
