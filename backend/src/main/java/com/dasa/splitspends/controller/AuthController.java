package com.dasa.splitspends.controller;

import com.dasa.splitspends.dto.AuthRequest;
import com.dasa.splitspends.dto.AuthResponse;
import com.dasa.splitspends.dto.GoogleAuthRequest;
import com.dasa.splitspends.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request));
    }
}