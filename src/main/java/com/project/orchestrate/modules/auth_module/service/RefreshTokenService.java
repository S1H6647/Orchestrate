package com.project.orchestrate.modules.auth_module.service;

import com.project.orchestrate.common.exception.InvalidTokenException;
import com.project.orchestrate.modules.user_module.model.User;
import com.project.orchestrate.modules.user_module.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final UserRepository userRepository;

    //    @Value("${}")
    private int expiryDays;

    public String generateRefreshToken() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public String createAndStore(User user) {
        String rawToken = generateRefreshToken();

        user.setRefreshToken(hash(rawToken));
        user.setRefreshTokenExpiresAt(LocalDateTime.now().plusDays(expiryDays));
        userRepository.save(user);

        return rawToken;
    }

    public User validateAndRotate(String rawToken) {
        String hashedToken = hash(rawToken);

        User user = userRepository.findByRefreshToken(hashedToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (user.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now())) {
            revokeToken(user);
            throw new InvalidTokenException("Refresh token expired. Please log in again.");
        }

        return user;
    }

    public void revokeToken(User user) {
        user.setRefreshToken(null);
        user.setRefreshTokenExpiresAt(null);
        userRepository.save(user);
    }
}
