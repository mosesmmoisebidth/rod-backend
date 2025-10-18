package org.chatapp.backend.auth;

import lombok.Data;

@Data
public class AuthRequest {
    // For registration
    private String firstName;
    private String lastName;
    private String username; // unique handle
    private String email;    // unique email

    // For login
    private String password;
}
