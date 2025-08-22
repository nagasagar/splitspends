package com.dasa.splitspends.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dasa.splitspends.entity.EmailVerificationToken;
import com.dasa.splitspends.entity.User;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByUser(User user);

    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken evt WHERE evt.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
