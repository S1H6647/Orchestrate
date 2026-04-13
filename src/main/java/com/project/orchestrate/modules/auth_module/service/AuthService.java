package com.project.orchestrate.modules.auth_module.service;

import com.project.orchestrate.common.exception.AccountNotVerifiedException;
import com.project.orchestrate.common.exception.AccountSuspendedException;
import com.project.orchestrate.common.exception.DuplicateResourceException;
import com.project.orchestrate.common.exception.InvalidCredentialsException;
import com.project.orchestrate.modules.auth_module.dto.*;
import com.project.orchestrate.modules.auth_module.mapper.UserMapper;
import com.project.orchestrate.modules.auth_module.messaging.AccountVerificationEvent;
import com.project.orchestrate.modules.auth_module.messaging.AccountVerificationEventPublisher;
import com.project.orchestrate.modules.user_module.model.User;
import com.project.orchestrate.modules.user_module.model.enums.AccountStatus;
import com.project.orchestrate.modules.user_module.model.enums.SystemRole;
import com.project.orchestrate.modules.user_module.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final AccountVerificationEventPublisher publisher;

    public RegisterResponse register(@Valid RegisterRequest request) {
        boolean exists = userRepository.existsByEmail(request.email());

        if (exists) {
            throw new DuplicateResourceException(
                    String.format("User with email %s already exists", request.email()));
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .status(AccountStatus.PENDING_VERIFICATION)
                .systemRole(SystemRole.USER)
                .verificationToken(randomUUIDToken())
                .verificationTokenExpiresAt(LocalDateTime.now().plusHours(24))
                .build();

        var saved = userRepository.save(user);

        publisher.publishAccountVerificationEvent(new AccountVerificationEvent(
                saved.getEmail(),
                saved.getName(),
                saved.getVerificationToken(),
                String.valueOf(saved.isEmailVerified())
        ));

        return userMapper.mapRegisterResponse(saved);
    }

    public LoginResponse login(@Valid LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isEmailVerified()) {
            throw new AccountNotVerifiedException("Please verify your email first");
        }

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountSuspendedException("Your account is not active");
        }

        user.setLastLoginAt(LocalDateTime.now());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createAndStore(user);

        return userMapper.mapLoginResponse(user, accessToken, refreshToken);
    }

    public LoginResponse refreshToken(String rawRefreshToken) {
        User user = refreshTokenService.validateAndRotate(rawRefreshToken);

        String accessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = refreshTokenService.createAndStore(user);

        return userMapper.mapLoginResponse(user, accessToken, newRefreshToken);
    }

    public void logout(User user) {
        refreshTokenService.revokeToken(user);
    }

    private String randomUUIDToken() {
        return UUID.randomUUID().toString();
    }

    public void verifyAccount(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new AccountNotVerifiedException("Invalid verification token"));

        if (user.getVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AccountNotVerifiedException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setStatus(AccountStatus.ACTIVE);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);

        userRepository.save(user);
    }

    public void resendVerificationEmail(ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AccountNotVerifiedException("User with this email does not exist"));

        if (user.isEmailVerified()) {
            throw new AccountNotVerifiedException("Email is already verified");
        }

        user.setVerificationToken(randomUUIDToken());
        user.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(24));

        userRepository.save(user);

        publisher.publishAccountVerificationEvent(new AccountVerificationEvent(
                user.getEmail(),
                user.getName(),
                user.getVerificationToken(),
                String.valueOf(user.isEmailVerified())
        ));
    }
}
