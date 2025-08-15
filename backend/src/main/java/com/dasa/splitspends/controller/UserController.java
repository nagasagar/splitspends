package com.dasa.splitspends.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dasa.splitspends.dto.UserProfileUpdateRequest;
import com.dasa.splitspends.dto.UserResponse;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        User user = userService.updateUserProfile(
                userId,
                request.getName(),
                request.getPhoneNumber(),
                request.getProfilePictureUrl());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        List<User> users = userService.searchUsers(query);
        List<UserResponse> response = users.stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long userId) {
        User user = userService.deactivateUser(userId);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PutMapping("/{userId}/reactivate")
    public ResponseEntity<UserResponse> reactivateUser(@PathVariable Long userId) {
        User user = userService.reactivateUser(userId);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @GetMapping("/{userId}/stats")
    public ResponseEntity<Object> getUserStats(@PathVariable Long userId) {
        Object stats = userService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }
}