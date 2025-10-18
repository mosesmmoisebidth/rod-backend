package org.chatapp.backend.auth;

import lombok.Data;

@Data
public class VerifyResetRequest {
    private String username;
    private String code; // 5-digit code
}