package com.eventhub.controller;

import com.eventhub.dto.auth.*;
import com.eventhub.entity.User;
import com.eventhub.service.AuthService;
import com.eventhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller handling user registration, login, and token management
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("User registration attempt for email: {}", request.getEmail());
        
        try {
            AuthResponse response = authService.register(request);
            log.info("User registered successfully: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Registration failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    @Operation(summary = "Authenticate user and return JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = (User) authentication.getPrincipal();
            AuthResponse response = authService.generateTokenResponse(user);
            
            // Update last login
            userService.updateLastLogin(user.getId());
            
            log.info("User logged in successfully: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    @Operation(summary = "Refresh JWT token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh attempt");
        
        try {
            AuthResponse response = authService.refreshToken(request.getRefreshToken());
            log.info("Token refreshed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw e;
        }
    }

    @Operation(summary = "Logout user and invalidate tokens")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        log.info("Logout attempt");
        
        try {
            authService.logout(request.getRefreshToken());
            log.info("User logged out successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Logout failed", e);
            throw e;
        }
    }

    @Operation(summary = "Request password reset")
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Password reset request for email: {}", request.getEmail());
        
        try {
            authService.initiatePasswordReset(request.getEmail());
            log.info("Password reset initiated for email: {}", request.getEmail());
            
            return ResponseEntity.ok(new MessageResponse(
                "If an account with that email exists, we've sent password reset instructions."
            ));
        } catch (Exception e) {
            log.error("Password reset request failed for email: {}", request.getEmail(), e);
            // Don't reveal if email exists or not
            return ResponseEntity.ok(new MessageResponse(
                "If an account with that email exists, we've sent password reset instructions."
            ));
        }
    }

    @Operation(summary = "Reset password with token")
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Password reset attempt with token");
        
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            log.info("Password reset successfully");
            
            return ResponseEntity.ok(new MessageResponse("Password has been reset successfully."));
        } catch (Exception e) {
            log.error("Password reset failed", e);
            throw e;
        }
    }

    @Operation(summary = "Verify email address")
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        log.info("Email verification attempt");
        
        try {
            authService.verifyEmail(request.getToken());
            log.info("Email verified successfully");
            
            return ResponseEntity.ok(new MessageResponse("Email verified successfully."));
        } catch (Exception e) {
            log.error("Email verification failed", e);
            throw e;
        }
    }

    @Operation(summary = "Resend email verification")
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        log.info("Resend verification request for email: {}", request.getEmail());
        
        try {
            authService.resendEmailVerification(request.getEmail());
            log.info("Verification email resent for: {}", request.getEmail());
            
            return ResponseEntity.ok(new MessageResponse(
                "If an account with that email exists and is unverified, we've sent a new verification email."
            ));
        } catch (Exception e) {
            log.error("Resend verification failed for email: {}", request.getEmail(), e);
            // Don't reveal if email exists or not
            return ResponseEntity.ok(new MessageResponse(
                "If an account with that email exists and is unverified, we've sent a new verification email."
            ));
        }
    }

    @Operation(summary = "Change password for authenticated user")
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        log.info("Password change request for user: {}", user.getEmail());
        
        try {
            authService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());
            log.info("Password changed successfully for user: {}", user.getEmail());
            
            return ResponseEntity.ok(new MessageResponse("Password changed successfully."));
        } catch (Exception e) {
            log.error("Password change failed for user: {}", user.getEmail(), e);
            throw e;
        }
    }

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        UserResponse userResponse = userService.convertToUserResponse(user);
        return ResponseEntity.ok(userResponse);
    }
}