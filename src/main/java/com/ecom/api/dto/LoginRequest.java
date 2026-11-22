package com.ecom.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for user login")
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username of the user", example = "partha.chakra2")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password of the user", example = "admin123")
    private String password;
}
