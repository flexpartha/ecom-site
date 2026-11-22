package com.ecom.api.service;

import com.ecom.api.dto.AuthResponse;
import com.ecom.api.dto.LoginRequest;
import com.ecom.api.dto.SignupRequest;
import com.ecom.api.dto.SignupResponse;
import com.ecom.api.entity.User;
import com.ecom.api.exception.DuplicateResourceException;
import com.ecom.api.repository.UserRepository;
import com.ecom.api.security.JwtService;
import com.ecom.api.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void should_returnSignupResponse_when_signupSuccessful() {
        // Arrange
        SignupRequest request = SignupRequest.builder()
                .firstName("Partha")
                .lastName("Chakraborty")
                .username("partha.chakra2")
                .password("admin123")
                .build();

        when(userRepository.existsByUsername("partha.chakra2")).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("encoded_pass");

        // Act
        SignupResponse response = authService.signup(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Registration successful");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void should_throwDuplicateResourceException_when_usernameAlreadyExistsOnSignup() {
        // Arrange
        SignupRequest request = SignupRequest.builder()
                .firstName("Partha")
                .lastName("Chakraborty")
                .username("partha.chakra2")
                .password("admin123")
                .build();

        when(userRepository.existsByUsername("partha.chakra2")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("partha.chakra2");
    }

    @Test
    void should_returnAuthResponse_when_loginSuccessful() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("partha.chakra2")
                .password("admin123")
                .build();

        User user = User.builder()
                .id(1L)
                .firstName("Partha")
                .lastName("Chakraborty")
                .username("partha.chakra2")
                .password("encoded_pass")
                .build();

        UserDetails userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByUsername("partha.chakra2")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mock-jwt-token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getFirstName()).isEqualTo("Partha");
        assertThat(response.getLastName()).isEqualTo("Chakraborty");
    }

    @Test
    void should_throwBadCredentialsException_when_invalidCredentialsOnLogin() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("partha.chakra2")
                .password("wrongpassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad credentials");
    }
}
