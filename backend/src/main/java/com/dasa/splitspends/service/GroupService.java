package com.dasa.splitspends.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.service.impl.GroupServiceImpl;

public interface GroupService {
        /**
         * Creates a new group.
         * 
         * @param name            the group name
         * @param description     the group description
         * @param createdByUserId the ID of the user creating the group
         * @param privacyLevel    the privacy level of the group
         * @param defaultCurrency the default currency for the group
         * @return the created Group entity
         */
        Group createGroup(String name, String description, Long createdByUserId, Group.PrivacyLevel privacyLevel,
                        String defaultCurrency);

        /**
         * Updates group information.
         * 
         * @param groupId         the group ID
         * @param name            the new group name
         * @param description     the new description
         * @param privacyLevel    the new privacy level
         * @param defaultCurrency the new default currency
         * @param updatedByUserId the ID of the user performing the update
         * @return the updated Group entity
         */
        Group updateGroup(Long groupId, String name, String description, Group.PrivacyLevel privacyLevel,
                        String defaultCurrency, Long updatedByUserId);

        /**
         * Uploads a new image for the group.
         * 
         * @param groupId          the group ID
         * @param file             the image file
         * @param uploadedByUserId the ID of the user uploading the image
         * @return the updated Group entity
         */
        Group uploadGroupImage(Long groupId, MultipartFile file, Long uploadedByUserId);

        /**
         * Adds members to a group.
         * 
         * @param groupId       the group ID
         * @param userIds       the list of user IDs to add
         * @param addedByUserId the ID of the user adding members
         * @return the updated Group entity
         */
        Group addMembers(Long groupId, List<Long> userIds, Long addedByUserId);

        /**
         * Removes a member from a group.
         * 
         * @param groupId         the group ID
         * @param userIdToRemove  the ID of the user to remove
         * @param removedByUserId the ID of the user performing the removal
         * @return the updated Group entity
         */
        Group removeMember(Long groupId, Long userIdToRemove, Long removedByUserId);

        /**
         * Promotes a user to admin in a group.
         * 
         * @param groupId          the group ID
         * @param userIdToPromote  the ID of the user to promote
         * @param promotedByUserId the ID of the user performing the promotion
         * @return the updated Group entity
         */
        Group promoteToAdmin(Long groupId, Long userIdToPromote, Long promotedByUserId);

        /**
         * Demotes an admin to a regular member in a group.
         * 
         * @param groupId         the group ID
         * @param userIdToDemote  the ID of the user to demote
         * @param demotedByUserId the ID of the user performing the demotion
         * @return the updated Group entity
         */
        Group demoteAdmin(Long groupId, Long userIdToDemote, Long demotedByUserId);

        /**
         * Retrieves a group by its ID.
         * 
         * @param groupId the group ID
         * @return the Group entity
         */
        Group getGroupById(Long groupId);

        /**
         * Retrieves all groups a user is a member of.
         * 
         * @param userId the user ID
         * @return a list of Group entities
         */
        List<Group> getUserGroups(Long userId);

        /**
         * Retrieves all groups created by a user.
         * 
         * @param userId the user ID
         * @return a list of Group entities
         */
        List<Group> getGroupsCreatedByUser(Long userId);

        /**
         * Searches for public groups matching a query.
         * 
         * @param searchQuery the search query
         * @return a list of matching Group entities
         */
        List<Group> searchPublicGroups(String searchQuery);

        /**
         * Retrieves all members of a group.
         * 
         * @param groupId the group ID
         * @return a set of User entities
         */
        Set<User> getGroupMembers(Long groupId);

        /**
         * Retrieves all admins of a group.
         * 
         * @param groupId the group ID
         * @return a set of User entities who are admins
         */
        Set<User> getGroupAdmins(Long groupId);

        /**
         * Archives a group.
         * 
         * @param groupId          the group ID
         * @param archivedByUserId the ID of the user performing the archive
         * @return the updated Group entity
         */
        Group archiveGroup(Long groupId, Long archivedByUserId);

        /**
         * Reactivates an archived group.
         * 
         * @param groupId             the group ID
         * @param reactivatedByUserId the ID of the user performing the reactivation
         * @return the updated Group entity
         */
        Group reactivateGroup(Long groupId, Long reactivatedByUserId);

        /**
         * Deletes a group.
         * 
         * @param groupId         the group ID
         * @param deletedByUserId the ID of the user performing the deletion
         */
        void deleteGroup(Long groupId, Long deletedByUserId);

        /**
         * Updates group settings.
         * 
         * @param groupId               the group ID
         * @param invitationPolicy      the new invitation policy
         * @param autoSettleThreshold   the new auto-settle threshold
         * @param allowExternalPayments whether to allow external payments
         * @param updatedByUserId       the ID of the user performing the update
         * @return the updated Group entity
         */
        Group updateGroupSettings(Long groupId, Group.InvitationPolicy invitationPolicy, BigDecimal autoSettleThreshold,
                        Boolean allowExternalPayments, Long updatedByUserId);

        /**
         * Retrieves statistics for a group.
         * 
         * @param groupId the group ID
         * @return a GroupStats DTO containing group statistics
         */
        GroupServiceImpl.GroupStats getGroupStats(Long groupId);

        /**
         * Retrieves balances for all users in a group.
         * 
         * @param groupId the group ID
         * @return a list of UserBalance DTOs
         */
        List<GroupServiceImpl.UserBalance> getGroupBalances(Long groupId);

        /**
         * Checks if a user is a member of a group.
         * 
         * @param userId  the user ID
         * @param groupId the group ID
         * @return true if the user is a member, false otherwise
         */
        boolean isUserMemberOfGroup(Long userId, Long groupId);

        /**
         * Validates that a user has access to a group.
         * 
         * @param groupId the group ID
         * @param userId  the user ID
         */
        void validateGroupAccess(Long groupId, Long userId);

        /**
         * Retrieves users eligible for group invitation based on a search query.
         * 
         * @param groupId     the group ID
         * @param searchQuery the search query
         * @return a list of User entities
         */
        List<User> getUsersForInvitation(Long groupId, String searchQuery);

        // ========== SUPER ADMIN METHODS ==========

        /**
         * Get all groups with pagination (super admin only).
         * 
         * @param pageable the pagination information
         * @return a page of Group entities
         */
        Page<Group> getAllGroups(Pageable pageable);

        /**
         * Search groups by name with pagination (super admin only).
         * 
         * @param searchQuery the search query
         * @param pageable    the pagination information
         * @return a page of matching Group entities
         */
        Page<Group> searchGroups(String searchQuery, Pageable pageable);

        /**
         * Get total group count (super admin only).
         * 
         * @return the total number of groups
         */
        Long getTotalGroupCount();

        /**
         * Get active group count (super admin only).
         * 
         * @return the number of active groups
         */
        Long getActiveGroupCount();
}