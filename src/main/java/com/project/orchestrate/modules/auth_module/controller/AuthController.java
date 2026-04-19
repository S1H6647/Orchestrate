package com.project.orchestrate.modules.auth_module.controller;

import com.project.orchestrate.modules.auth_module.dto.*;
import com.project.orchestrate.modules.auth_module.security.user.UserPrincipal;
import com.project.orchestrate.modules.auth_module.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/v1/auth/register
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    // POST /api/v1/auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // GET /api/v1/auth/verify?token={token}
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyAccount(@RequestParam("token") String token) {
        authService.verifyAccount(token);
        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Account verified successfully"
                )
        );
    }

    // GET /api/v1/auth/resend-verification?email={email}
    @GetMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerificationEmail(
            @RequestParam("email") String email
    ) {
        authService.resendVerificationEmail(new ResendVerificationRequest(email));
        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Verification email resent successfully"
                )
        );
    }

    // GET /api/v1/auth/refresh-token?refreshToken={refreshToken}
    @GetMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(
            @RequestParam("refreshToken") String refreshToken
    ) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    // GET /api/v1/auth/me
    @GetMapping("/me")
    public ResponseEntity<MeResponse> getMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(authService.getMe(userPrincipal));
    }

    // GET /api/v1/auth?refreshToken={refreshToken}
    @GetMapping
    public ResponseEntity<Map<String, Object>> logout(
            @RequestParam("refreshToken") String refreshToken
    ) {
        authService.logout(refreshToken);
        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Logged out successfully"
                )
        );
    }
}
