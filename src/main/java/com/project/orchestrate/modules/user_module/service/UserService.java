package com.project.orchestrate.modules.user_module.service;

import com.project.orchestrate.common.exception.ResourceNotFoundException;
import com.project.orchestrate.common.service.HelperMethodService;
import com.project.orchestrate.modules.user_module.dto.UserDetailResponse;
import com.project.orchestrate.modules.user_module.dto.UserResponse;
import com.project.orchestrate.modules.user_module.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final HelperMethodService helper;

    public UserDetailResponse getUserById(UUID userId) {
        return userRepository.findById(userId)
                .map(UserDetailResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public Page<UserResponse> getAllUsers(String q, Pageable pageable) {
        String normalizedQuery = helper.normalizeSearchQuery(q);
        return userRepository.searchUsers(normalizedQuery, pageable)
                .map(UserResponse::from);
    }
}
