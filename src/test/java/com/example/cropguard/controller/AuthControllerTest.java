package com.example.cropguard.controller;

import com.example.cropguard.config.AppProperties;
import com.example.cropguard.dto.LoginRequest;
import com.example.cropguard.dto.LoginResponse;
import com.example.cropguard.security.JwtService;
import com.example.cropguard.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(com.example.cropguard.config.SecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;
    @MockBean AuthService authService;
    @MockBean JwtService jwtService;
    @MockBean AppProperties appProperties;

    @Test
    void login_validCredentials_returnsToken() throws Exception {
        when(authService.login(any())).thenReturn(
            new LoginResponse("jwt-token", 1L, "Max", "ASSESSOR"));

        mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new LoginRequest("max@bauernhof.de", "passwort123"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.role").value("ASSESSOR"));
    }

    @Test
    void login_blankPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new LoginRequest("max@bauernhof.de", ""))))
            .andExpect(status().isBadRequest());
    }
}
