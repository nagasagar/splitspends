package com.dasa.splitspends.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;

public interface UserService {

    /**
     * Registers a new user with the given name, email, and password.
     * 
     * @param name     the user's name
     * @param email    the user's email
     * @param password the user's raw password
     * @return the created User entity
     */
    User registerUser(String name, String email, String password);

    /**
     * Registers or logs in a user using Google authentication.
     * 
     * @param googleId the Google account ID
     * @param email    the user's email
     * @param name     the user's name
     * @return the created or existing User entity
     */
    User registerOrLoginWithGoogle(String googleId, String email, String name);

    /**
     * Verifies the user's email address.
     * 
     * @param email the user's email
     * @return the updated User entity
     */
    User verifyEmail(String email);

    /**
     * Authenticates a user with email and password.
     * 
     * @param email    the user's email
     * @param password the user's raw password
     * @return the authenticated User entity
     */
    User loginUser(String email, String password);

    /**
     * Updates the user's profile information.
     * 
     * @param userId            the user's ID
     * @param name              the new name
     * @param phoneNumber       the new phone number
     * @param preferredCurrency the preferred currency
     * @param timezone          the timezone
     * @param language          the language
     * @return the updated User entity
     */
    User updateProfile(Long userId, String name, String phoneNumber, String preferredCurrency, String timezone,
            String language);

    /**
     * Updates the user's notification preferences.
     * 
     * @param userId             the user's ID
     * @param emailNotifications enable/disable email notifications
     * @param pushNotifications  enable/disable push notifications
     * @param paymentReminders   enable/disable payment reminders
     * @return the updated User entity
     */
    User updateNotificationPreferences(Long userId, Boolean emailNotifications, Boolean pushNotifications,
            Boolean paymentReminders);

    /**
     * Uploads a new profile picture for the user.
     * 
     * @param userId the user's ID
     * @param file   the profile picture file
     * @return the updated User entity
     */
    User uploadProfilePicture(Long userId, MultipartFile file);

    /**
     * Changes the user's password.
     * 
     * @param userId          the user's ID
     * @param currentPassword the current password
     * @param newPassword     the new password
     * @return the updated User entity
     */
    User changePassword(Long userId, String currentPassword, String newPassword);

    /**
     * Retrieves a user by their ID.
     * 
     * @param userId the user's ID
     * @return the User entity
     */
    User getUserById(Long userId);

    /**
     * Retrieves a user by their email.
     * 
     * @param email the user's email
     * @return an Optional containing the User entity if found
     */
    Optional<User> getUserByEmail(String email);

    /**
     * Searches for users matching the given query.
     * 
     * @param query the search query
     * @return a list of matching users
     */
    List<User> searchUsers(String query);

    /**
     * Retrieves users by a list of IDs.
     * 
     * @param userIds the list of user IDs
     * @return a list of User entities
     */
    List<User> getUsersByIds(List<Long> userIds);

    /**
     * Retrieves a paginated list of all active users.
     * 
     * @param pageable the pagination information
     * @return a page of User entities
     */
    Page<User> getAllActiveUsers(Pageable pageable);

    /**
     * Suspends a user account with a reason.
     * 
     * @param userId the user's ID
     * @param reason the reason for suspension
     * @return the updated User entity
     */
    User suspendUser(Long userId, String reason);

    /**
     * Reactivates a suspended user account.
     * 
     * @param userId the user's ID
     * @return the updated User entity
     */
    User reactivateUser(Long userId);

    /**
     * Soft deletes a user account.
     * 
     * @param userId the user's ID
     */
    void deleteUser(Long userId);

    /**
     * Retrieves all groups the user is a member of.
     * 
     * @param userId the user's ID
     * @return a list of Group entities
     */
    List<Group> getUserGroups(Long userId);

    /**
     * Checks if a user is a member of a specific group.
     * 
     * @param userId  the user's ID
     * @param groupId the group's ID
     * @return true if the user is a member, false otherwise
     */
    boolean isUserMemberOfGroup(Long userId, Long groupId);

    /**
     * Retrieves mutual groups between two users.
     * 
     * @param user1Id the first user's ID
     * @param user2Id the second user's ID
     * @return a list of mutual Group entities
     */
    List<Group> getMutualGroups(Long user1Id, Long user2Id);

    /**
     * Retrieves statistics for a user.
     * 
     * @param userId the user's ID
     * @return a UserStats DTO containing user statistics
     */
    UserStats getUserStats(Long userId);

    /**
     * Checks if an email is available for registration.
     * 
     * @param email the email to check
     * @return true if available, false otherwise
     */
    boolean isEmailAvailable(String email);

    /**
     * Validates that a user account is active.
     * 
     * @param userId the user's ID
     */
    void validateUserActiveStatus(Long userId);

    /**
     * Retrieves users eligible for group invitation based on a search query and
     * group.
     * 
     * @param searchQuery the search query
     * @param groupId     the group ID
     * @return a list of User entities
     */
    List<User> getUsersForGroupInvitation(String searchQuery, Long groupId);

    /**
     * Updates the user's profile information including name, phone number, and
     * profile picture URL.
     *
     * @param userId            the user's ID
     * @param name              the new name (nullable)
     * @param phoneNumber       the new phone number (nullable)
     * @param profilePictureUrl the new profile picture URL (nullable)
     * @return the updated User entity
     */
    User updateUserProfile(Long userId, String name, String phoneNumber, String profilePictureUrl);

    /**
     * Deactivates a user account by setting its status to INACTIVE.
     *
     * @param userId the user's ID
     * @return the updated User entity
     */
    User deactivateUser(Long userId);

    // ========== SUPER ADMIN METHODS ==========

    /**
     * Get all users with pagination (super admin only).
     * 
     * @param pageable the pagination information
     * @return a page of User entities
     */
    Page<User> getAllUsers(Pageable pageable);

    /**
     * Search users by name or email with pagination (super admin only).
     * 
     * @param searchQuery the search query
     * @param pageable    the pagination information
     * @return a page of matching User entities
     */
    Page<User> searchUsers(String searchQuery, Pageable pageable);

    /**
     * Get total user count (super admin only).
     * 
     * @return the total number of users
     */
    Long getTotalUserCount();

    /**
     * Get active user count (super admin only).
     * 
     * @return the number of active users
     */
    Long getActiveUserCount();

    /**
     * Reset user password and generate temporary password (super admin only).
     * 
     * @param userId the user's ID
     * @return the temporary password
     */
    String resetPassword(Long userId);

    /**
     * DTO for user statistics.
     */
    @lombok.Builder
    @lombok.Data
    public static class UserStats {
        private Long userId;
        private Long totalGroups;
        private Long totalExpensesPaid;
        private Long totalExpenseSplits;
        private Long accountAge; // in days
        private Boolean isVerified;
        private Boolean isGoogleUser;
        private LocalDateTime lastLoginAt;
    }

}
