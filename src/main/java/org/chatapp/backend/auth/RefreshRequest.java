package org.chatapp.backend.auth;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}
