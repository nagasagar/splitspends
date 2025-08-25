package com.dasa.splitspends.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // ========== BASIC AUTHENTICATION QUERIES ==========

    // Find user by email (for login)
    Optional<User> findByEmail(String email);

    // Find user by Google ID (for Google SSO)
    Optional<User> findByGoogleId(String googleId);

    // ========== USER SEARCH & DISCOVERY ==========

    // Search users by name or email (for adding to groups)
    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);

    // Search users by name or email with pagination (for super admin)
    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);

    // Find users by name only (alternative search)
    List<User> findByNameContainingIgnoreCase(String name);

    // Find users by email pattern (alternative search)
    List<User> findByEmailContainingIgnoreCase(String email);

    // ========== USER STATUS & FILTERING ==========

    // Find active users with pagination (excluding soft deleted)
    Page<User> findByAccountStatusAndDeletedAtIsNull(User.AccountStatus status, Pageable pageable);

    // Find users by account status
    List<User> findByAccountStatus(User.AccountStatus status);

    // Find users who need email verification
    List<User> findByEmailVerifiedFalse();

    // ========== GROUP RELATIONSHIP QUERIES ==========

    // Find users who are NOT members of a specific group (for invitations)
    @Query("SELECT u FROM User u WHERE u.accountStatus = 'ACTIVE' AND u.deletedAt IS NULL " +
            "AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :searchQuery, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) " +
            "AND u NOT IN (SELECT m FROM Group g JOIN g.members m WHERE g = :group)")
    List<User> findUsersNotInGroup(@Param("searchQuery") String searchQuery, @Param("group") Group group);

    // Find users who are members of a specific group
    @Query("SELECT u FROM User u JOIN u.groups g WHERE g = :group")
    List<User> findByGroupMembership(@Param("group") Group group);

    // ========== STATISTICS & ANALYTICS ==========

    // Count active users
    Long countByAccountStatusAndDeletedAtIsNull(User.AccountStatus status);

    // Count users by verification status
    Long countByEmailVerified(Boolean verified);

    // ========== VALIDATION HELPERS ==========

    // Check if email exists (for registration validation)
    boolean existsByEmail(String email);

    // Check if Google ID exists (for SSO validation)
    boolean existsByGoogleId(String googleId);
}
