package org.chatapp.backend.auth;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String username;
    private String code; // 5-digit code
    private String newPassword;
}