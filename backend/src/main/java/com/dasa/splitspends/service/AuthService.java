package com.dasa.splitspends.service;

import com.dasa.splitspends.dto.auth.AuthRequest;
import com.dasa.splitspends.dto.auth.AuthResponse;
import com.dasa.splitspends.dto.auth.GoogleAuthRequest;

public interface AuthService {

    /**
     * Register a new user with email and password.
     * 
     * @param request the signup request (email, password, etc.)
     * @return AuthResponse with JWT and user info
     */
    AuthResponse signup(AuthRequest request);

    /**
     * Authenticate a user with email and password.
     * 
     * @param request the login request (email, password)
     * @return AuthResponse with JWT and user info
     */
    AuthResponse login(AuthRequest request);

    /**
     * Authenticate or register a user using Google SSO.
     * 
     * @param request the Google auth request (idToken)
     * @return AuthResponse with JWT and user info
     */
    AuthResponse googleLogin(GoogleAuthRequest request);
}
