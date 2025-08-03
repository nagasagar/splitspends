package com.dasa.splitspends.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dasa.splitspends.dto.AuthRequest;
import com.dasa.splitspends.dto.AuthResponse;
import com.dasa.splitspends.dto.GoogleAuthRequest;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.UserRepository;
import com.dasa.splitspends.security.JwtUtil;
import com.dasa.splitspends.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthResponse signup(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // set other user fields as needed
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, null, user.getEmail());
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        Object principal = authentication.getPrincipal();
        String email;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            email = (String) principal;
        } else {
            throw new UsernameNotFoundException("Unable to extract user email from authentication principal");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, null, user.getEmail());
    }

    @Override
    public AuthResponse googleLogin(GoogleAuthRequest request) {
        // TODO: Verify Google ID token and extract user info
        // For now, just a placeholder
        String email = "googleuser@example.com"; // Extracted from Google token
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    // set other fields as needed
                    return userRepository.save(newUser);
                });
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, null, user.getEmail());
    }
}
