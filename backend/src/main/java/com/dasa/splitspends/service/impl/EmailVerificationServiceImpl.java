package com.dasa.splitspends.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.dasa.splitspends.entity.EmailVerificationToken;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.EmailVerificationTokenRepository;
import com.dasa.splitspends.repository.UserRepository;
import com.dasa.splitspends.service.EmailVerificationService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.verification-token-expiry}")
    private long tokenExpiryMs;
    @Value("${app.base-url}")
    private String baseUrl;
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(User user) {
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalStateException("User is already verified");
        }
        // Delete any existing token
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
        // Generate new token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(tokenExpiryMs / 1000);
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .build();
        tokenRepository.save(verificationToken);
        // Send email
        try {
            sendEmailNotification(user, token);
            user.setEmailVerificationSentAt(LocalDateTime.now());
            userRepository.save(user);
        } catch (Exception e) {
            // Delete token if email fails
            tokenRepository.delete(verificationToken);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public VerificationResult verifyEmail(String token) {
        Optional<EmailVerificationToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return VerificationResult.INVALID_TOKEN;
        }
        EmailVerificationToken verificationToken = tokenOpt.get();
        if (verificationToken.isExpired()) {
            tokenRepository.delete(verificationToken);
            return VerificationResult.EXPIRED_TOKEN;
        }
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        // Delete the used token
        tokenRepository.delete(verificationToken);
        return VerificationResult.SUCCESS;
    }

    @Override
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalStateException("Email is already verified");
        }
        // Rate limiting: allow resend only after 5 minutes
        if (user.getEmailVerificationSentAt() != null &&
                user.getEmailVerificationSentAt().isAfter(LocalDateTime.now().minusMinutes(5))) {
            throw new IllegalStateException("Please wait 5 minutes before requesting another verification email");
        }
        sendVerificationEmail(user);
    }

    private void sendEmailNotification(User user, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("Verify Your Email - SplitSpends");
            helper.setFrom("noreply@splitspends.com");
            // Create verification URL
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;
            // Use Thymeleaf template
            Context context = new Context();
            context.setVariable("userName", user.getName());
            context.setVariable("verificationUrl", verificationUrl);
            context.setVariable("expiryHours", tokenExpiryMs / (1000 * 60 * 60));
            String htmlContent = templateEngine.process("email/verification-email", context);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

}
