package com.piisw.tod.controller;

import com.piisw.tod.BaseIntegrationTest;
import com.piisw.tod.dto.auth.LoginRequest;
import com.piisw.tod.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends BaseIntegrationTest {

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("john.smith@example.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("john.smith@example.com"));
    }

    @Test
    void shouldFailLoginWithWrongPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest("john.smith@example.com", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("new.user@example.com", "newpassword123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("new.user@example.com"));
    }
}