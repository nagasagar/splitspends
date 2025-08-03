package com.dasa.splitspends.dto;

public class AuthResponse {
    private String token;
    private String refreshToken;
    private String email;

    public AuthResponse(String token, String refreshToken, String email) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
