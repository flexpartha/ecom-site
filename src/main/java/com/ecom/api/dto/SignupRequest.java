package com.ecom.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for user registration")
public class SignupRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "First name of the user", example = "Partha")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "Last name of the user", example = "Chakraborty")
    private String lastName;

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must not exceed 100 characters")
    @Schema(description = "Unique username for the account", example = "partha.chakra2")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "Password (minimum 6 characters)", example = "admin123")
    private String password;
}
