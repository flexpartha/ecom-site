package com.ecom.api.controller;

import com.ecom.api.dto.AuthResponse;
import com.ecom.api.dto.LoginRequest;
import com.ecom.api.dto.SignupRequest;
import com.ecom.api.dto.SignupResponse;
import com.ecom.api.exception.DuplicateResourceException;
import com.ecom.api.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@WithMockUser
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void should_return200Ok_when_loginSuccessful() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("partha.chakra2")
                .password("admin123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token-123")
                .tokenType("Bearer")
                .firstName("Partha")
                .lastName("Chakraborty")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.firstName").value("Partha"))
                .andExpect(jsonPath("$.lastName").value("Chakraborty"))
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.expiresIn").doesNotExist());
    }

    @Test
    void should_return200Ok_when_signupSuccessful() throws Exception {
        // Arrange
        SignupRequest request = SignupRequest.builder()
                .firstName("Partha")
                .lastName("Chakraborty")
                .username("partha.chakra2")
                .password("admin123")
                .build();

        SignupResponse response = SignupResponse.builder()
                .message("Registration successful")
                .build();

        when(authService.signup(any(SignupRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    @Test
    void should_return400BadRequest_when_loginValidationFails() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("")
                .password("")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void should_return409Conflict_when_signupDuplicateUsername() throws Exception {
        // Arrange
        SignupRequest request = SignupRequest.builder()
                .firstName("Partha")
                .lastName("Chakraborty")
                .username("partha.chakra2")
                .password("admin123")
                .build();

        when(authService.signup(any(SignupRequest.class)))
                .thenThrow(new DuplicateResourceException("Username 'partha.chakra2' is already taken"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username 'partha.chakra2' is already taken"));
    }

    @Test
    void should_return401Unauthorized_when_loginBadCredentials() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("partha.chakra2")
                .password("wrongpass")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }
}
