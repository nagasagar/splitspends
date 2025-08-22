package com.dasa.splitspends.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dasa.splitspends.service.EmailVerificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class EmailVerificationController {
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            EmailVerificationService.VerificationResult result = emailVerificationService.verifyEmail(token);
            switch (result) {
                case SUCCESS:
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Email verified successfully"));
                case EXPIRED_TOKEN:
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Verification token has expired"));
                case INVALID_TOKEN:
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Invalid verification token"));
                default:
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Verification failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Verification failed: " + e.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            emailVerificationService.resendVerificationEmail(email);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Verification email sent successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to send verification email"));
        }
    }
}
