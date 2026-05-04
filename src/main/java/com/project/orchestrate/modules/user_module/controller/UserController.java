package com.project.orchestrate.modules.user_module.controller;

import com.project.orchestrate.common.dto.PageResponse;
import com.project.orchestrate.modules.user_module.dto.UserDetailResponse;
import com.project.orchestrate.modules.user_module.dto.UserResponse;
import com.project.orchestrate.modules.user_module.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/v1/users/{userId}
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailResponse> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // GET /api/v1/users/all
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) String q
    ) {
        String sortProperty = sortBy;
        Sort.Direction sortDirection = Sort.Direction.DESC;

        if (sortBy != null && sortBy.contains(",")) {
            String[] sortParts = sortBy.split(",", 2);
            sortProperty = sortParts[0].trim();
            if (sortParts.length > 1 && !sortParts[1].isBlank()) {
                sortDirection = Sort.Direction.fromString(sortParts[1].trim());
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortProperty));

        return ResponseEntity.ok(
                PageResponse.from(userService.getAllUsers(q, pageable))
        );
    }


}
