package com.dasa.splitspends.service;

import com.dasa.splitspends.entity.User;

/**
 * Service for handling email verification logic, including token generation,
 * email sending, verification, resending, and cleanup of expired tokens.
 */
public interface EmailVerificationService {
    /**
     * Result of the email verification process.
     */
    enum VerificationResult {
        SUCCESS,
        INVALID_TOKEN,
        EXPIRED_TOKEN
    }

    /**
     * Generates a new verification token for the user, sends a verification email,
     * and updates the user's verification sent timestamp.
     *
     * @param user the user to send the verification email to
     * @throws IllegalStateException if the user is already verified
     * @throws RuntimeException      if email sending fails
     */
    void sendVerificationEmail(User user);

    /**
     * Verifies the user's email using the provided token.
     *
     * @param token the verification token
     * @return the result of the verification (success, invalid, or expired)
     */
    VerificationResult verifyEmail(String token);

    /**
     * Resends the verification email to the user, with rate limiting.
     *
     * @param email the user's email address
     * @throws IllegalStateException                       if the user is already
     *                                                     verified or rate limit
     *                                                     exceeded
     * @throws jakarta.persistence.EntityNotFoundException if the user is not found
     */
    void resendVerificationEmail(String email);

    /**
     * Deletes all expired email verification tokens. Intended to be run as a
     * scheduled task.
     */
    void cleanupExpiredTokens();
}
