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
@Schema(description = "Response returned upon user registration")
public class SignupResponse {

    @Schema(description = "HTTP status code", example = "200")
    @Builder.Default
    private int status = 200;

    @Schema(description = "Status message", example = "Registration successful")
    private String message;
}
