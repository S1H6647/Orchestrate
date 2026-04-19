package com.project.orchestrate.modules.organization_module.controller;

import com.project.orchestrate.modules.auth_module.security.user.UserPrincipal;
import com.project.orchestrate.modules.organization_module.dto.*;
import com.project.orchestrate.modules.organization_module.model.enums.OrganizationScope;
import com.project.orchestrate.modules.organization_module.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    // GET /api/v1/organizations/all
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<OrganizationResponse>> getOrganizations() {
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    // GET /api/v1/organizations/my-organizations
    @GetMapping("/my-organizations")
    public ResponseEntity<List<CurrentUserOrganizationResponse>> getMyOrganizations(
            @RequestParam(defaultValue = "ALL") OrganizationScope scope,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(organizationService.getMyOrganizations(userPrincipal.getId(), scope));
    }

    // GET /api/v1/organizations/me
    @GetMapping("/me")
    public ResponseEntity<CurrentUserOrganizationResponse> getCurrentOrganizationUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(organizationService.getCurrentOrganizationUser(userPrincipal));
    }

    // GET /api/v1/organizations/{organizationId}
    @GetMapping("/{organizationId}")
    @PreAuthorize("@securityService.hasOrgRole(#organizationId, 'OWNER', 'ADMIN', 'MEMBER', 'VIEWER')")
    public ResponseEntity<OrganizationResponse> getOrganizationById(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(organizationService.getOrganizationById(organizationId, userPrincipal.getId()));
    }

    // POST /api/v1/organizations
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OrganizationResponse> createOrganization(
            @RequestPart("request") @Valid CreateOrganizationRequest request,
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Creating organization with name: {} by user: {}", request.name(), userPrincipal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(organizationService.createOrganization(request, image, userPrincipal.getId()));
    }

    // PATCH /api/v1/organizations/{organizationId}/profile
    @PatchMapping(value = "/{organizationId}/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasOrgRole(#organizationId, 'OWNER', 'ADMIN')")
    public ResponseEntity<OrganizationResponse> updateOrganizationProfile(
            @PathVariable UUID organizationId,
            @Valid @RequestPart("request") UpdateOrganizationProfileRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(
                organizationService.updateOrganizationProfile(organizationId, request, image, userPrincipal.getId())
        );
    }

    // PATCH /api/v1/organizations/{organizationId}/identity
    @PatchMapping("/{organizationId}/identity")
    @PreAuthorize("@securityService.hasOrgRole(#organizationId, 'OWNER', 'ADMIN')")
    public ResponseEntity<OrganizationResponse> updateOrganizationIdentity(
            @PathVariable UUID organizationId,
            @Valid @RequestBody UpdateOrganizationIdentityRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(
                organizationService.updateOrganizationIdentity(organizationId, request, userPrincipal.getId())
        );
    }

    // DELETE /api/v1/organizations?organizationId={organizationId}
    @DeleteMapping
    @PreAuthorize("@securityService.hasOrgRole(#organizationId, 'OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteOrganization(
            @RequestParam UUID organizationId,
            @Valid @RequestBody ConfirmDeleteOrganizationRequest request
    ) {
        organizationService.deleteOrganization(organizationId, request);
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/organizations/{organizationId}/members
    @GetMapping("/{organizationId}/members")
    @PreAuthorize("@securityService.hasOrgRole(#organizationId, 'OWNER', 'ADMIN', 'MEMBER', 'VIEWER')")
    public ResponseEntity<List<OrganizationMemberResponse>> getOrganizationMembers(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(organizationService.getOrganizationMembers(organizationId));
    }

    // PATCH /api/v1/organizations/{organizationId}/members/{userId}/role
    @PatchMapping("/{organizationId}/members/{userId}/role")
    @PreAuthorize("@securityService.hasOrgRole(#organizationId, 'OWNER', 'ADMIN')")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable UUID organizationId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateOrganizationMemberRoleRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        organizationService.updateMemberRole(organizationId, userId, request, userPrincipal);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/v1/organizations/{organizationId}/members/{userId}
    @PatchMapping("/{organizationId}/members/{userId}")
    @PreAuthorize("@securityService.hasOrgRole(#organizationId, 'OWNER', 'ADMIN')")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID organizationId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        organizationService.removeMember(organizationId, userId, userPrincipal);
        return ResponseEntity.noContent().build();
    }

    // POST /api/v1/organizations/{organizationId}/members/{userId}/restore
    @PostMapping("/{organizationId}/members/{userId}/restore")
    @PreAuthorize("@securityService.hasOrgRole(#organizationId, 'OWNER', 'ADMIN')")
    public ResponseEntity<Void> restoreMember(
            @PathVariable UUID organizationId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        organizationService.restoreMember(organizationId, userId, userPrincipal);
        return ResponseEntity.noContent().build();
    }

    // POST /api/v1/organizations/{organizationId}/invitations
    @PostMapping("/{organizationId}/invitations")
    @PreAuthorize("@securityService.hasOrgRole(#organizationId, 'OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> inviteUserToOrganization(
            @PathVariable UUID organizationId,
            @Valid @RequestBody InviteUserToOrganizationRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        organizationService.inviteUserToOrganization(organizationId, request, userPrincipal);
        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "User successfully invited"
                )
        );
    }

    // GET /api/v1/organizations/{organizationId}/invitations
    @GetMapping("/{organizationId}/invitations")
    @PreAuthorize("@securityService.hasOrgRole(#organizationId, 'OWNER', 'ADMIN')")
    public ResponseEntity<List<OrganizationInvitationResponse>> getPendingInvitations(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(organizationService.getPendingInvitations(organizationId, userPrincipal.getId()));
    }

    // POST /api/v1/organizations/{organizationId}/invitations/{inviteId}/resend
    @PostMapping("/{organizationId}/invitations/{inviteId}/resend")
    @PreAuthorize("@securityService.hasOrgRole(#organizationId, 'OWNER', 'ADMIN')")
    public ResponseEntity<OrganizationInvitationResponse> resendInvitationEmail(
            @PathVariable UUID organizationId,
            @PathVariable UUID inviteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(
                organizationService.resendInvitationEmail(organizationId, inviteId, userPrincipal.getId())
        );
    }

    // DELETE /api/v1/organizations/{organizationId}/invitations/{inviteId}
    @DeleteMapping("/{organizationId}/invitations/{inviteId}")
    @PreAuthorize("@securityService.hasOrgRole(#organizationId, 'OWNER', 'ADMIN')")
    public ResponseEntity<Void> cancelInvitation(
            @PathVariable UUID organizationId,
            @PathVariable UUID inviteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        organizationService.cancelInvitation(organizationId, inviteId, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/organizations/invitations/accept?token={token}
    @GetMapping("/invitations/accept")
    public ResponseEntity<MemberAddedToOrganizationResponse> acceptOrganizationInvite(
            @RequestParam("token") String token
    ) {
        return ResponseEntity.ok(organizationService.acceptOrganizationInvite(token));
    }

    // GET /api/v1/organizations/invitations/me
    @GetMapping("/invitations/me")
    public ResponseEntity<List<MyInvitationResponse>> getMyInvitations(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(organizationService.getMyInvitations(userPrincipal.getId()));
    }

    // POST /api/v1/organizations/invitations/{token}/validate
    @PostMapping("/invitations/{token}/validate")
    public ResponseEntity<Map<String, Object>> validateInvitationToken(
            @PathVariable String token
    ) {
        organizationService.validateInvitationToken(token);
        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Invitation token is valid"
                )
        );
    }

    // POST /api/v1/organizations/invitations/{token}/decline
    @PostMapping("/invitations/{token}/decline")
    public ResponseEntity<Void> declineInvitation(
            @PathVariable String token,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        organizationService.declineInvitation(token, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }

    // POST /api/v1/organizations/{organizationId}/transfer-ownership
    @PostMapping("/{organizationId}/transfer-ownership")
    @PreAuthorize("@securityService.hasOrgRole(#organizationId, 'OWNER', 'ADMIN')")
    public ResponseEntity<Void> transferOrganizationOwnership(
            @PathVariable UUID organizationId,
            @Valid @RequestBody TransferOrganizationOwnershipRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        organizationService.transferOrganizationOwnership(organizationId, request, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }

    // POST /api/v1/organizations/{organizationId}/members/bulk-invite
    // GET /api/v1/organizations/{organizationId}/usage-metrics
    // ?status=&role=&q=&page=&size=
    // POST /api/v1/organizations/{organizationId}/leave
}
