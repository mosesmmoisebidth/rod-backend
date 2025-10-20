package org.chatapp.backend.auth;

import lombok.RequiredArgsConstructor;
import org.chatapp.backend.security.JwtService;
import org.chatapp.backend.user.User;
import org.chatapp.backend.user.UserRepository;
import org.chatapp.backend.user.UserStatus;
import org.chatapp.backend.utils.FileUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    private static final int RESET_CODE_LENGTH = 5;
    private static final int MAX_ATTEMPTS = 5;

    public AuthResponse register(AuthRequest request) {
        // Basic validations
        if (request.getUsername() == null || request.getUsername().isBlank())
            throw new IllegalArgumentException("Username is required");
        if (request.getEmail() == null || request.getEmail().isBlank())
            throw new IllegalArgumentException("Email is required");
        if (request.getPassword() == null || request.getPassword().isBlank())
            throw new IllegalArgumentException("Password is required");

        // Uniqueness checks
        if (userRepository.findById(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create user with provided registration data
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .status(UserStatus.OFFLINE)
                .lastLogin(LocalDateTime.now())
                .verified(false)
                .build();
        userRepository.save(user);

        // Issue verification code and email to the provided email address
        String code = generateNumericCode(6);
        VerificationToken vt = VerificationToken.builder()
                .token(code)
                .username(user.getUsername())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .attempts(0)
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        verificationTokenRepository.save(vt);
        // Send email asynchronously to avoid blocking the request in case SMTP is slow/unavailable
        emailService.sendHtmlAsync(
                user.getEmail(),
                "Verify your email",
                emailService.buildVerificationEmail(user.getUsername(), code)
        );

        // Return tokens optionally, but user won’t be allowed to access protected endpoints until verified
        String access = jwtService.generateAccessToken(user.getUsername());
        String refresh = jwtService.generateRefreshToken(user.getUsername());
        return new AuthResponse(access, refresh);
    }

    public AuthResponse login(LoginRequest request) {
        // Identifier can be username or email
        String identifier = request.getUsername();
        User user = userRepository.findById(identifier)
                .orElseGet(() -> userRepository.findByEmail(identifier).orElse(null));
        if (user == null) throw new BadCredentialsException("Invalid credentials");
        if (Boolean.FALSE.equals(user.getVerified())) {
            throw new DisabledException("Account not verified. Please check your email for the verification code.");
        }
        // Authenticate against the actual username stored
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            // Migration fallback: if existing user has plaintext password stored, accept once and upgrade to encoded
            if (request.getPassword() != null && request.getPassword().equals(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
            } else {
                throw e;
            }
        }
        // On success, issue tokens for the canonical username
        String access = jwtService.generateAccessToken(user.getUsername());
        String refresh = jwtService.generateRefreshToken(user.getUsername());
        return new AuthResponse(access, refresh);
    }

    public AuthResponse refresh(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);
        if (!jwtService.isRefreshToken(refreshToken) || !jwtService.isTokenValid(refreshToken, username)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        // Ensure user is verified
        User u = userRepository.findById(username).orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if (Boolean.FALSE.equals(u.getVerified())) {
            throw new DisabledException("Account not verified");
        }
        String newAccess = jwtService.generateAccessToken(username);
        String newRefresh = jwtService.generateRefreshToken(username);
        return new AuthResponse(newAccess, newRefresh);
    }

    public void verifyEmail(String username, String code) {
        VerificationToken vt = verificationTokenRepository.findTopByUsernameOrderByCreatedAtDesc(username)
                .orElseThrow(() -> new IllegalArgumentException("No verification request for this user"));
        if (Boolean.TRUE.equals(vt.getUsed())) throw new IllegalStateException("Code already used");
        if (vt.getExpiresAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("Code expired");
        if (vt.getAttempts() >= MAX_ATTEMPTS) throw new IllegalStateException("Too many attempts");
        if (!vt.getToken().equals(code)) {
            vt.setAttempts(vt.getAttempts() + 1);
            verificationTokenRepository.save(vt);
            throw new BadCredentialsException("Invalid code");
        }
        vt.setUsed(true);
        verificationTokenRepository.save(vt);
        User user = userRepository.findById(vt.getUsername()).orElseThrow();
        user.setVerified(true);
        userRepository.save(user);
        // cleanup: remove other tokens for this user
        verificationTokenRepository.deleteByUsername(user.getUsername());
    }

    public void forgotPassword(String username) {
        // rate limiting: allow a new code only if last token older than 60 seconds
        PasswordResetToken last = passwordResetTokenRepository.findTopByUsernameOrderByCreatedAtDesc(username).orElse(null);
        if (last != null && last.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(60))) {
            throw new IllegalStateException("Please wait before requesting another code");
        }
        String code = generateNumericCode(RESET_CODE_LENGTH);
        PasswordResetToken t = PasswordResetToken.builder()
                .username(username)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .attempts(0)
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        passwordResetTokenRepository.save(t);
        emailService.sendHtml(username, "Password reset code", emailService.buildResetCodeEmail(username, code));
    }

    public void verifyReset(String username, String code) {
        PasswordResetToken t = passwordResetTokenRepository.findTopByUsernameOrderByCreatedAtDesc(username)
                .orElseThrow(() -> new IllegalArgumentException("No reset request for this user"));
        if (Boolean.TRUE.equals(t.getUsed())) throw new IllegalStateException("Code already used");
        if (t.getExpiresAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("Code expired");
        if (t.getAttempts() >= MAX_ATTEMPTS) throw new IllegalStateException("Too many attempts");
        if (!t.getCode().equals(code)) {
            t.setAttempts(t.getAttempts() + 1);
            passwordResetTokenRepository.save(t);
            throw new BadCredentialsException("Invalid code");
        }
    }

    public void resetPassword(String username, String code, String newPassword) {
        PasswordResetToken t = passwordResetTokenRepository.findTopByUsernameOrderByCreatedAtDesc(username)
                .orElseThrow(() -> new IllegalArgumentException("No reset request for this user"));
        if (Boolean.TRUE.equals(t.getUsed())) throw new IllegalStateException("Code already used");
        if (t.getExpiresAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("Code expired");
        if (t.getAttempts() >= MAX_ATTEMPTS) throw new IllegalStateException("Too many attempts");
        if (!t.getCode().equals(code)) {
            t.setAttempts(t.getAttempts() + 1);
            passwordResetTokenRepository.save(t);
            throw new BadCredentialsException("Invalid code");
        }
        t.setUsed(true);
        passwordResetTokenRepository.save(t);
        User user = userRepository.findById(username).orElseThrow();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private String generateNumericCode(int length) {
        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
