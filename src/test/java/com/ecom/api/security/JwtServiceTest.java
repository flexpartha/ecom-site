package com.ecom.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
    }

    @Test
    void should_generateValidToken_when_userDetailsProvided() {
        // Arrange
        UserDetails userDetails = new User("partha.chakra2", "password", Collections.emptyList());

        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void should_extractCorrectUsername_when_tokenIsValid() {
        // Arrange
        UserDetails userDetails = new User("partha.chakra2", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertThat(extractedUsername).isEqualTo("partha.chakra2");
    }

    @Test
    void should_returnTrue_when_tokenIsValidForUser() {
        // Arrange
        UserDetails userDetails = new User("partha.chakra2", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    void should_returnFalse_when_usernameDoesNotMatch() {
        // Arrange
        UserDetails userDetails = new User("partha.chakra2", "password", Collections.emptyList());
        UserDetails otherUser = new User("other.user", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, otherUser);

        // Assert
        assertThat(isValid).isFalse();
    }
}
