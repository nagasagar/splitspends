package com.dasa.splitspends.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.GroupRepository;
import com.dasa.splitspends.repository.UserRepository;
import com.dasa.splitspends.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ========== USER REGISTRATION & AUTHENTICATION ==========

    /**
     * Register a new user with email and password
     */
    @Override
    public User registerUser(String name, String email, String password) {
        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create new user
        User user = User.builder()
                .name(name)
                .email(email.toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(password))
                .emailVerified(false)
                .accountStatus(User.AccountStatus.PENDING_VERIFICATION)
                .build();

        return userRepository.save(user);
    }

    /**
     * Register or login user via Google SSO
     */
    @Override
    public User registerOrLoginWithGoogle(String googleId, String email, String name) {
        // Try to find existing user by Google ID
        Optional<User> existingUser = userRepository.findByGoogleId(googleId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.updateLastLogin();
            return userRepository.save(user);
        }

        // Try to find by email (user might have registered with email before)
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            User user = userByEmail.get();
            user.setGoogleId(googleId);
            user.setEmailVerified(true);
            user.setAccountStatus(User.AccountStatus.ACTIVE);
            user.updateLastLogin();
            return userRepository.save(user);
        }

        // Create new Google user
        User newUser = User.builder()
                .name(name)
                .email(email.toLowerCase().trim())
                .googleId(googleId)
                .emailVerified(true)
                .accountStatus(User.AccountStatus.ACTIVE)
                .build();

        newUser.updateLastLogin();
        return userRepository.save(newUser);
    }

    /**
     * Verify user email
     */
    @Override
    public User verifyEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEmailVerified(true);
        if (user.getAccountStatus() == User.AccountStatus.PENDING_VERIFICATION) {
            user.setAccountStatus(User.AccountStatus.ACTIVE);
        }

        return userRepository.save(user);
    }

    /**
     * Login user (update last login timestamp)
     */
    @Override
    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.isActive()) {
            throw new IllegalStateException("Account is not active");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        user.updateLastLogin();
        return userRepository.save(user);
    }

    // ========== USER PROFILE MANAGEMENT ==========

    /**
     * Update user profile
     */
    @Override
    public User updateProfile(Long userId, String name, String phoneNumber,
            String preferredCurrency, String timezone, String language) {
        User user = getUserById(userId);

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name);
        }
        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
        }
        if (preferredCurrency != null) {
            user.setPreferredCurrency(preferredCurrency);
        }
        if (timezone != null) {
            user.setTimezone(timezone);
        }
        if (language != null) {
            user.setLanguage(language);
        }

        return userRepository.save(user);
    }

    /**
     * Update user notification preferences
     */
    @Override
    public User updateNotificationPreferences(Long userId, Boolean emailNotifications,
            Boolean pushNotifications, Boolean paymentReminders) {
        User user = getUserById(userId);

        if (emailNotifications != null) {
            user.setEmailNotifications(emailNotifications);
        }
        if (pushNotifications != null) {
            user.setPushNotifications(pushNotifications);
        }
        if (paymentReminders != null) {
            user.setPaymentReminders(paymentReminders);
        }

        return userRepository.save(user);
    }

    /**
     * Upload profile picture
     */
    @Override
    public User uploadProfilePicture(Long userId, MultipartFile file) {
        User user = getUserById(userId);

        // TODO: Implement file upload to cloud storage (AWS S3, etc.)
        // For now, just store filename
        String filename = userId + "_" + file.getOriginalFilename();
        user.setProfilePictureUrl("/uploads/profiles/" + filename);

        return userRepository.save(user);
    }

    /**
     * Change password
     */
    @Override
    public User changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);

        if (user.getPasswordHash() == null) {
            throw new IllegalStateException("User registered with Google SSO cannot change password");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    // ========== USER RETRIEVAL ==========

    /**
     * Get user by ID
     */
    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    /**
     * Get user by email
     */
    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Search users by name or email (for adding to groups)
     */
    @Override
    public List<User> searchUsers(String query) {
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }

    /**
     * Get users by IDs (for group operations)
     */
    @Override
    public List<User> getUsersByIds(List<Long> userIds) {
        return userRepository.findAllById(userIds);
    }

    /**
     * Get all active users with pagination
     */
    @Override
    public Page<User> getAllActiveUsers(Pageable pageable) {
        return userRepository.findByAccountStatusAndDeletedAtIsNull(User.AccountStatus.ACTIVE, pageable);
    }

    // ========== USER ACCOUNT MANAGEMENT ==========

    /**
     * Suspend user account
     */
    @Override
    public User suspendUser(Long userId, String reason) {
        User user = getUserById(userId);
        user.setAccountStatus(User.AccountStatus.SUSPENDED);
        return userRepository.save(user);
    }

    /**
     * Reactivate suspended user
     */
    @Override
    public User reactivateUser(Long userId) {
        User user = getUserById(userId);
        if (user.getAccountStatus() == User.AccountStatus.SUSPENDED) {
            user.setAccountStatus(User.AccountStatus.ACTIVE);
        }
        return userRepository.save(user);
    }

    /**
     * Soft delete user account
     */
    @Override
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        user.softDelete();
        userRepository.save(user);
    }

    // ========== GROUP RELATIONSHIPS ==========

    /**
     * Get user's groups
     */
    @Override
    public List<Group> getUserGroups(Long userId) {
        User user = getUserById(userId);
        return groupRepository.findByMembersContaining(user);
    }

    /**
     * Check if user is member of a group
     */
    @Override
    public boolean isUserMemberOfGroup(Long userId, Long groupId) {
        User user = getUserById(userId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        return user.isMemberOf(group);
    }

    /**
     * Get mutual groups between two users
     */
    @Override
    public List<Group> getMutualGroups(Long user1Id, Long user2Id) {
        User user1 = getUserById(user1Id);
        User user2 = getUserById(user2Id);

        return groupRepository.findMutualGroups(user1, user2);
    }

    // ========== USER STATISTICS ==========

    /**
     * Get user statistics
     */
    @Override
    public UserStats getUserStats(Long userId) {
        User user = getUserById(userId);

        // Get counts from repositories
        Long totalGroups = groupRepository.countByMembersContaining(user);
        // You'll need to add these methods to repositories:
        // Long totalExpensesPaid = expenseRepository.countByPaidBy(user);
        // Long totalExpenseSplits = expenseSplitRepository.countByUser(user);

        return UserStats.builder()
                .userId(userId)
                .totalGroups(totalGroups)
                .accountAge(java.time.temporal.ChronoUnit.DAYS.between(user.getJoinedAt(), LocalDateTime.now()))
                .isVerified(user.isVerified())
                .isGoogleUser(user.isGoogleUser())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    // ========== UTILITY METHODS ==========

    /**
     * Check if email is available for registration
     */
    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.findByEmail(email).isPresent();
    }

    /**
     * Validate user exists and is active
     */
    @Override
    public void validateUserActiveStatus(Long userId) {
        User user = getUserById(userId);
        if (!user.isActive()) {
            throw new IllegalStateException("User account is not active");
        }
    }

    /**
     * Get users for group invitation (exclude existing members)
     */
    @Override
    public List<User> getUsersForGroupInvitation(String searchQuery, Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        return userRepository.findUsersNotInGroup(searchQuery, group);
    }

    @Override
    public User updateUserProfile(Long userId, String name, String phoneNumber, String profilePictureUrl) {
        User user = getUserById(userId);
        if (name != null && !name.trim().isEmpty()) {
            user.setName(name);
        }
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            user.setPhoneNumber(phoneNumber);
        }
        if (profilePictureUrl != null && !profilePictureUrl.trim().isEmpty()) {
            user.setProfilePictureUrl(profilePictureUrl);
        }
        return userRepository.save(user);
    }

    @Override
    public User deactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setAccountStatus(User.AccountStatus.INACTIVE);
        return userRepository.save(user);
    }
}