package org.chatapp.backend.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    @Schema(description = "Username or email")
    private String username;

    @NotBlank
    private String password;
}
