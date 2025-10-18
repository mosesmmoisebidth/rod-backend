package org.chatapp.backend.auth;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String username; // email address
}