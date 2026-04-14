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
@RequestMapping("/api/v1/auth/")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyAccount(@RequestParam("token") String token) {
        authService.verifyAccount(token);
        return ResponseEntity.ok(
                Map.of("message", "Account verified successfully")
        );
    }

    @GetMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(
            @RequestParam("email") String email
    ) {
        authService.resendVerificationEmail(new ResendVerificationRequest(email));
        return ResponseEntity.ok(
                Map.of("message", "Verification email resent successfully")
        );
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(
            @RequestParam("refreshToken") String refreshToken
    ) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> logout(
            @RequestParam("refreshToken") String refreshToken
    ) {
        authService.logout(refreshToken);
        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully")
        );
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> getMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(authService.getMe(userPrincipal));
    }
}