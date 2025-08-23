package com.dasa.splitspends.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSignupAndLogin() throws Exception {
        // Signup
        Map<String, String> signupRequest = new HashMap<>();
        signupRequest.put("name", "Test User");
        signupRequest.put("email", "testuser@example.com");
        signupRequest.put("password", "password123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // Login
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "testuser@example.com");
        loginRequest.put("password", "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        assertThat(loginResponse).contains("token");
    }

    @Test
    void testDuplicateSignup() throws Exception {
        Map<String, String> signupRequest = new HashMap<>();
        signupRequest.put("name", "User1");
        signupRequest.put("email", "dup@example.com");
        signupRequest.put("password", "password123");

        // First signup should succeed
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // Second signup with same email should fail (usually 4xx)
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testSignupWithMissingFields() throws Exception {
        Map<String, String> signupRequest = new HashMap<>();
        signupRequest.put("email", "missingname@example.com");
        signupRequest.put("password", "password123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testSignupWithWeakPassword() throws Exception {
        Map<String, String> signupRequest = new HashMap<>();
        signupRequest.put("name", "Weak Password");
        signupRequest.put("email", "weakpass@example.com");
        signupRequest.put("password", "123"); // too short

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testLoginWithWrongPassword() throws Exception {
        // First, signup
        Map<String, String> signupRequest = new HashMap<>();
        signupRequest.put("name", "Wrong Password");
        signupRequest.put("email", "wrongpass@example.com");
        signupRequest.put("password", "correctpass");
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // Try login with wrong password
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "wrongpass@example.com");
        loginRequest.put("password", "wrongpass");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testLoginWithNonExistentUser() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "nouser@example.com");
        loginRequest.put("password", "anyPassword");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testLoginWithMissingFields() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "missingpass@example.com");
        // No password
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }
}
