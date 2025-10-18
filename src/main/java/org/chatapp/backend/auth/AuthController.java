package org.chatapp.backend.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/auth")
@Tag(name = "Authentication", description = "User registration, login and token refresh")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Requires firstName, lastName, username, email and password. Sends a 6‑digit email verification code and returns JWT tokens (access to protected APIs requires verification).", security = {})
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Provide username (or email) and password only. Returns access and refresh tokens once the account is verified.", security = {})
    public ResponseEntity<AuthResponse> login(@RequestBody @jakarta.validation.Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh tokens", description = "Exchanges a valid refresh token for a new access token (and refresh token)", security = {})
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify email", description = "Confirms a user's email using the 6‑digit code sent after registration.", security = {})
    public ResponseEntity<String> verify(@RequestBody VerifyResetRequest request) {
        authService.verifyEmail(request.getUsername(), request.getCode());
        return ResponseEntity.ok("Email verified successfully. You can now log in.");
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Sends a 5-digit code to the user's email. Rate limited to avoid abuse.", security = {})
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-reset")
    @Operation(summary = "Verify password reset code", description = "Checks if the provided 5-digit code is valid and not expired.", security = {})
    public ResponseEntity<Void> verifyReset(@RequestBody VerifyResetRequest request) {
        authService.verifyReset(request.getUsername(), request.getCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets password when a valid (verified) 5-digit code is provided.", security = {})
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getUsername(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
