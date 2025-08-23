package com.dasa.splitspends.dto.auth;

public class GoogleAuthRequest {
    private String idToken; // Google ID token

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
