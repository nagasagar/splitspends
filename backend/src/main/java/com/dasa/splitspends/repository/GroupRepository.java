package com.dasa.splitspends.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;

public interface GroupRepository extends JpaRepository<Group, Long> {

        // ========== USER-GROUP RELATIONSHIP QUERIES ==========

        // Find all groups a particular user is a member of (by user ID)
        List<Group> findByMembers_Id(Long userId);

        // Find all groups a particular user is a member of (by user object)
        List<Group> findByMembersContaining(User user);

        // Find all groups created by a particular user
        List<Group> findByCreatedBy(User user);

        // ========== GROUP SEARCH & DISCOVERY ==========

        // Search by partial name match (for group search feature)
        List<Group> findByNameContainingIgnoreCase(String namePart);

        // Search by partial name match with pagination (for super admin)
        Page<Group> findByNameContainingIgnoreCase(String namePart, Pageable pageable);

        // Find groups by exact name (for validation)
        List<Group> findByName(String name);

        // ========== MUTUAL GROUP QUERIES ==========

        // Find mutual groups between two users
        @Query("SELECT g FROM Group g JOIN g.members m1 JOIN g.members m2 " +
                        "WHERE m1 = :user1 AND m2 = :user2")
        List<Group> findMutualGroups(@Param("user1") User user1, @Param("user2") User user2);

        // Find groups where user1 is member but user2 is not
        @Query("SELECT g FROM Group g JOIN g.members m1 " +
                        "WHERE m1 = :user1 AND :user2 NOT IN (SELECT m2 FROM g.members m2)")
        List<Group> findGroupsWhereUser1IsMemberButNotUser2(@Param("user1") User user1, @Param("user2") User user2);

        // ========== GROUP STATISTICS ==========

        // Count groups a user is member of
        Long countByMembersContaining(User user);

        // Count groups created by a user
        Long countByCreatedBy(User user);

        // Count groups by status (for super admin)
        Long countByStatusAndDeletedAtIsNull(Group.GroupStatus status);

        // Get groups with most members (for recommendations)
        @Query("SELECT g FROM Group g ORDER BY SIZE(g.members) DESC")
        List<Group> findGroupsOrderedByMemberCount();

        // ========== GROUP VALIDATION ==========

        // Check if group name exists (for validation)
        boolean existsByName(String name);

        // Check if user is member of group
        @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END " +
                        "FROM Group g JOIN g.members m WHERE g.id = :groupId AND m.id = :userId")
        boolean isUserMemberOfGroup(@Param("groupId") Long groupId, @Param("userId") Long userId);

        // ========== ADMIN & MANAGEMENT QUERIES ==========

        // Find groups with specific member count
        @Query("SELECT g FROM Group g WHERE SIZE(g.members) = :memberCount")
        List<Group> findGroupsWithMemberCount(@Param("memberCount") int memberCount);

        // Find groups with more than specified member count
        @Query("SELECT g FROM Group g WHERE SIZE(g.members) > :memberCount")
        List<Group> findGroupsWithMoreThanMembers(@Param("memberCount") int memberCount);

        // Find empty groups (for cleanup)
        @Query("SELECT g FROM Group g WHERE SIZE(g.members) = 0")
        List<Group> findEmptyGroups();
}