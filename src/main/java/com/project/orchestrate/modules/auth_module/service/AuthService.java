package com.project.orchestrate.modules.auth_module.service;

import com.project.orchestrate.common.exception.AccountNotVerifiedException;
import com.project.orchestrate.common.exception.AccountSuspendedException;
import com.project.orchestrate.common.exception.DuplicateResourceException;
import com.project.orchestrate.common.exception.InvalidCredentialsException;
import com.project.orchestrate.modules.auth_module.dto.LoginRequest;
import com.project.orchestrate.modules.auth_module.dto.LoginResponse;
import com.project.orchestrate.modules.auth_module.dto.RegisterRequest;
import com.project.orchestrate.modules.auth_module.dto.RegisterResponse;
import com.project.orchestrate.modules.auth_module.mapper.UserMapper;
import com.project.orchestrate.modules.user_module.model.User;
import com.project.orchestrate.modules.user_module.model.enums.AccountStatus;
import com.project.orchestrate.modules.user_module.model.enums.SystemRole;
import com.project.orchestrate.modules.user_module.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

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
                .build();

        var saved = userRepository.save(user);
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


}
