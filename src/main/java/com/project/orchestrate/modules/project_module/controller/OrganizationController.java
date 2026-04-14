package com.project.orchestrate.modules.project_module.controller;

import com.project.orchestrate.modules.auth_module.security.user.UserPrincipal;
import com.project.orchestrate.modules.project_module.dto.CreateOrganizationRequest;
import com.project.orchestrate.modules.project_module.dto.OrganizationResponse;
import com.project.orchestrate.modules.project_module.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    public final OrganizationService organizationService;

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<OrganizationResponse>> getOrganizations() {
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<OrganizationResponse> getMyOrganizations(@PathVariable UUID userId) {
        return ResponseEntity.ok(organizationService.getMyOrganization(userId));
    }

    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Creating organization with name: {} by user: {}", request.name(), userPrincipal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(organizationService.createOrganization(request, userPrincipal.getId()));
    }

}
