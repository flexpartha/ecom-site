package com.ecom.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing login authentication details")
public class AuthResponse {

    @Schema(description = "JWT access token")
    private String token;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;

    @Schema(description = "First name of the user", example = "Partha")
    private String firstName;

    @Schema(description = "Last name of the user", example = "Chakraborty")
    private String lastName;
}
