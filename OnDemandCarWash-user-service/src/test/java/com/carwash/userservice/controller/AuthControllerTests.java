package com.carwash.userservice.controller;

import com.carwash.userservice.dto.AuthResponse;
import com.carwash.userservice.dto.LoginRequest;
import com.carwash.userservice.dto.RegisterRequest;
import com.carwash.userservice.entity.Role;
import com.carwash.userservice.security.CustomUserDetailsService;
import com.carwash.userservice.security.JwtAuthenticationFilter;
import com.carwash.userservice.security.JwtTokenProvider;
import com.carwash.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void register_ValidRequest_ReturnsCreated() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .password("secret123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token-sample")
                .id(1L)
                .email("john@example.com")
                .fullName("John Doe")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        when(userService.registerUser(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.token").value("jwt-token-sample"));
    }

    @Test
    void login_ValidRequest_ReturnsOk() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("john@example.com")
                .password("secret123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token-sample")
                .id(1L)
                .email("john@example.com")
                .fullName("John Doe")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        when(userService.loginUser(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.token").value("jwt-token-sample"));
    }
}
