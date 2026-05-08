package com.project.orchestrate.modules.notification_module.controller;

import com.project.orchestrate.common.dto.PageResponse;
import com.project.orchestrate.modules.auth_module.security.user.UserPrincipal;
import com.project.orchestrate.modules.notification_module.dto.NotificationResponse;
import com.project.orchestrate.modules.notification_module.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping()
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false", required = false) boolean unread,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        return ResponseEntity.ok(
                PageResponse.from(notificationService.getAllNotifications(user.getId(), pageable, unread))
        );
    }

    // PATCH /notifications/{id}/read
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID notificationId
    ) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // PATCH /notifications/read-all
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
