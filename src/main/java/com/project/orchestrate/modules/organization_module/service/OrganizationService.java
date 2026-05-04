package com.project.orchestrate.modules.organization_module.service;

import com.project.orchestrate.common.exception.AccessDeniedException;
import com.project.orchestrate.common.exception.DuplicateResourceException;
import com.project.orchestrate.common.exception.ResourceNotFoundException;
import com.project.orchestrate.common.service.HelperMethodService;
import com.project.orchestrate.common.service.StorageService;
import com.project.orchestrate.modules.auth_module.security.user.UserPrincipal;
import com.project.orchestrate.modules.notification_module.service.EmailService;
import com.project.orchestrate.modules.organization_module.config.OrganizationPlanProperties;
import com.project.orchestrate.modules.organization_module.dto.*;
import com.project.orchestrate.modules.organization_module.mapper.OrganizationMapper;
import com.project.orchestrate.modules.organization_module.model.Organization;
import com.project.orchestrate.modules.organization_module.model.OrganizationMember;
import com.project.orchestrate.modules.organization_module.model.enums.OrganizationRole;
import com.project.orchestrate.modules.organization_module.model.enums.OrganizationScope;
import com.project.orchestrate.modules.organization_module.model.enums.OrganizationStatus;
import com.project.orchestrate.modules.organization_module.model.enums.Plan;
import com.project.orchestrate.modules.organization_module.repository.OrganizationMemberRepository;
import com.project.orchestrate.modules.organization_module.repository.OrganizationRepository;
import com.project.orchestrate.modules.project_module.repository.ProjectRepository;
import com.project.orchestrate.modules.user_module.model.User;
import com.project.orchestrate.modules.user_module.model.enums.MemberStatus;
import com.project.orchestrate.modules.user_module.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.project.orchestrate.common.service.HelperMethodService.randomUUIDToken;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final OrganizationMapper organizationMapper;
    private final StorageService storageService;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationPlanProperties organizationPlanProperties;
    private final EmailService emailService;
    private final HelperMethodService helper;
    private final ProjectRepository projectRepository;

    @Value("${app.invitation.expiry-hours}")
    private long invitationExpiryHours;

    @Transactional
    public OrganizationResponse createOrganization(
            @Valid CreateOrganizationRequest request,
            MultipartFile image,
            UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String slug = request.slug();
        if (slug == null || slug.isBlank()) {
            slug = helper.generateSlug(request.name());
        }

        String logoUrl = storageService.upload(image);
        Plan selectedPlan = Plan.FREE;
        var limits = organizationPlanProperties.getLimitOrThrow(selectedPlan);

        Organization organization = Organization.builder()
                .name(request.name())
                .slug(slug)
                .description(request.description())
                .logoUrl(logoUrl)
                .websiteUrl(request.websiteUrl())
                .taskSequence(0L)
                .createdBy(user)
                .maxMembers(limits.getMaxMembers())
                .maxProjects(limits.getMaxProjects())
                .plan(selectedPlan)
                .status(OrganizationStatus.ACTIVE)
                .build();

        OrganizationMember member = OrganizationMember.builder()
                .user(user)
                .organization(organization)
                .role(OrganizationRole.OWNER)
                .status(MemberStatus.ACTIVE)
                .build();

        var saved = organizationRepository.save(organization);
        organizationMemberRepository.save(member);
        return organizationMapper.mapToOrganizationResponse(saved);
    }

    @Transactional
    public Page<OrganizationResponse> getAllOrganizations(String q, Pageable pageable) {
        String normalizedQuery = helper.normalizeSearchQuery(q);
        return organizationRepository.searchOrganizations(normalizedQuery, pageable)
                .map(organizationMapper::mapToOrganizationResponse);
    }

    @Transactional(readOnly = true)
    public List<CurrentUserOrganizationResponse> getMyOrganizations(UUID userId, OrganizationScope scope) {
        List<OrganizationMember> membership = switch (scope) {
            case ALL -> organizationMemberRepository.findAllByUserIdAndStatusWithOrganization(
                    userId,
                    MemberStatus.ACTIVE
            );
            case OWNED -> organizationMemberRepository.findAllByUserIdAndStatusAndRoleWithOrganization(
                    userId,
                    MemberStatus.ACTIVE,
                    OrganizationRole.OWNER
            );
            case SHARED -> organizationMemberRepository.findAllByUserIdAndStatusAndRoleNotWithOrganization(
                    userId,
                    MemberStatus.ACTIVE,
                    OrganizationRole.OWNER
            );
        };

        return membership.stream()
                .map(CurrentUserOrganizationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<OrganizationMemberResponse> getOrganizationMembers(
            UUID organizationId,
            String status,
            String role,
            String query,
            Pageable pageable
    ) {
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        MemberStatus memberStatusFilter = parseMemberStatus(status);
        OrganizationRole roleFilter = parseOrganizationRole(role);
        String normalizedQuery = helper.normalizeSearchQuery(query);

        return organizationMemberRepository
                .findMembersByFilters(organizationId, memberStatusFilter, roleFilter, normalizedQuery, pageable)
                .map(OrganizationMemberResponse::from);
    }

    private MemberStatus parseMemberStatus(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return null;
        }

        try {
            return MemberStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status. Allowed values: ALL, ACTIVE, INVITED, REMOVED");
        }
    }

    private OrganizationRole parseOrganizationRole(String role) {
        if (role == null || role.isBlank() || "ALL".equalsIgnoreCase(role)) {
            return null;
        }

        try {
            return OrganizationRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role. Allowed values: ALL, OWNER, ADMIN, MEMBER, VIEWER");
        }
    }

    @Transactional
    public void deleteOrganization(UUID organizationId, @Valid ConfirmDeleteOrganizationRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        String slug = request.confirmation().substring(7);

        if (!slug.equals(organization.getSlug())) {
            throw new IllegalArgumentException("Confirmation does not match organization slug");
        }

        organizationRepository.delete(organization);

        emailService.sendOrganizationDeletionEmail(
                organization.getCreatedBy().getEmail(),
                organization.getCreatedBy().getName(),
                organization.getName()
        );
    }

    public void inviteUserToOrganization(
            UUID organizationId,
            @Valid InviteUserToOrganizationRequest request,
            UserPrincipal userPrincipal) {

        String token = randomUUIDToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(invitationExpiryHours);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        OrganizationMember member = organizationMemberRepository
                .findByOrganizationIdAndUserId(organizationId, user.getId())
                .map(existing -> {
                    if (existing.getStatus() == MemberStatus.ACTIVE) {
                        throw new DuplicateResourceException("User is already member");
                    }

                    if (existing.getStatus() == MemberStatus.INVITED &&
                            existing.getInviteExpiresAt() != null &&
                            existing.getInviteExpiresAt().isAfter(LocalDateTime.now())) {
                        throw new DuplicateResourceException("User already invited");
                    }

                    existing.setRole(OrganizationRole.valueOf(request.role()));
                    existing.setStatus(MemberStatus.INVITED);
                    existing.setInviteToken(token);
                    existing.setInviteExpiresAt(expiresAt);
                    existing.setJoinedAt(null);
                    return existing;
                })
                .orElseGet(() -> OrganizationMember.builder()
                        .user(user)
                        .organization(organization)
                        .role(OrganizationRole.valueOf(request.role()))
                        .status(MemberStatus.INVITED)
                        .inviteToken(token)
                        .inviteExpiresAt(expiresAt)
                        .joinedAt(null)
                        .build());

        organizationMemberRepository.save(member);

        emailService.sendOrganizationInvitationEmail(
                user.getEmail(),
                user.getName(),
                userPrincipal.getUsername(),
                organization.getName(),
                request.role(),
                token
        );
    }

    @Transactional
    public MemberAddedToOrganizationResponse acceptOrganizationInvite(String token) {
        OrganizationMember member = organizationMemberRepository.findByInviteToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        member.setStatus(MemberStatus.ACTIVE);
        member.setInviteToken(null);
        member.setInviteExpiresAt(null);
        member.setJoinedAt(LocalDateTime.now());

        return MemberAddedToOrganizationResponse.from(member);
    }

    @Transactional
    public void removeMember(
            UUID organizationId,
            UUID userId,
            UserPrincipal currentUser) {
        var memberToRemove = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this organization"));

        var higherMember = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user is not a member of this organization"));

        log.info("Attempting to remove member with role: {} by user with role: {}",
                memberToRemove.getRole(), higherMember.getRole());

        // Only allow if current user has strictly higher role than the member to remove
        if (higherMember.getRole().ordinal() >= memberToRemove.getRole().ordinal()) {
            throw new IllegalArgumentException("You don't have permission to remove this member");
        }

        memberToRemove.setStatus(MemberStatus.REMOVED);
        memberToRemove.setInviteToken(null);
        memberToRemove.setInviteExpiresAt(null);
    }

    @Transactional
    public void updateMemberRole(
            UUID organizationId,
            UUID userId,
            @Valid UpdateOrganizationMemberRoleRequest request,
            UserPrincipal currentUser) {
        var memberToUpdate = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this organization"));

        var higherMember = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user is not a member of this organization"));

        // Only allow if current user has strictly higher role than the member to update
        if (higherMember.getRole().ordinal() >= memberToUpdate.getRole().ordinal()) {
            throw new IllegalArgumentException("You don't have permission to update roles for this member");
        }

        memberToUpdate.setRole(OrganizationRole.valueOf(request.role()));
    }

    public CurrentUserOrganizationResponse getCurrentOrganizationUser(UserPrincipal userPrincipal) {
        OrganizationMember member = organizationMemberRepository.findByUserIdAndStatus(userPrincipal.getId(), MemberStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("You are not an active member of any organization"));

        return CurrentUserOrganizationResponse.from(member);
    }

    @Transactional
    public OrganizationResponse updateOrganizationProfile(
            UUID organizationId,
            @Valid UpdateOrganizationProfileRequest request,
            MultipartFile image,
            UUID userId
    ) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        OrganizationMember member = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this organization"));

        if (member.getRole() != OrganizationRole.OWNER && member.getRole() != OrganizationRole.ADMIN) {
            throw new AccessDeniedException("You don't have permission to update organization profile");
        }

        if (request.description() != null) {
            organization.setDescription(request.description().trim());
        }

        if (request.websiteUrl() != null) {
            organization.setWebsiteUrl(request.websiteUrl().trim());
        }

        if (image != null && !image.isEmpty()) {
            organization.setLogoUrl(storageService.upload(image));
        }

        Organization updated = organizationRepository.save(organization);
        return organizationMapper.mapToOrganizationResponse(updated);
    }

    @Transactional
    public OrganizationResponse updateOrganizationIdentity(
            UUID organizationId,
            @Valid UpdateOrganizationIdentityRequest request,
            UUID userId
    ) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        OrganizationMember member = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this organization"));

        if (member.getRole() != OrganizationRole.OWNER) {
            throw new AccessDeniedException("Only the owner can update organization identity");
        }

        if (request.name() != null) {
            String trimmedName = request.name().trim();
            if (trimmedName.isEmpty()) {
                throw new IllegalArgumentException("Organization name cannot be blank");
            }
            organization.setName(trimmedName);
        }

        if (request.slug() != null) {
            String trimmedSlug = request.slug().trim();
            if (trimmedSlug.isEmpty()) {
                throw new IllegalArgumentException("Organization slug cannot be blank");
            }

            if (organizationRepository.existsBySlugAndIdNot(trimmedSlug, organizationId)) {
                throw new DuplicateResourceException("Organization slug already exists");
            }

            organization.setSlug(trimmedSlug);
        }

        Organization updated = organizationRepository.save(organization);
        return organizationMapper.mapToOrganizationResponse(updated);
    }

    @Transactional
    public void restoreMember(UUID organizationId, UUID userId, UserPrincipal userPrincipal) {
        var memberToRestore = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this organization"));

        var higherMember = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user is not a member of this organization"));

        // Only allow if current user has strictly higher role than the member to restore
        if (higherMember.getRole().ordinal() >= memberToRestore.getRole().ordinal()) {
            throw new IllegalArgumentException("You don't have permission to restore this member");
        }

        memberToRestore.setStatus(MemberStatus.ACTIVE);
        log.info("Member with userId: {} restored to organizationId: {}", userId, organizationId);
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(UUID organizationId, UUID userId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (organizationMemberRepository.existsByOrganizationIdAndUserId(organizationId, userId)) {
            return organizationMapper.mapToOrganizationResponse(organization);
        } else {
            throw new AccessDeniedException("You are not a member of this organization");
        }
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationBySlug(String slug, UserPrincipal userPrincipal) {
        Organization organization = organizationRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (organizationMemberRepository.existsByOrganizationIdAndUserId(organization.getId(), userPrincipal.getId())) {
            return organizationMapper.mapToOrganizationResponse(organization);
        }

        if (userPrincipal != null && "SYSTEM_ADMIN".equals(userPrincipal.getRole())) {
            return organizationMapper.mapToOrganizationResponse(organization);
        }

        throw new AccessDeniedException("You are not a member of this organization");
    }

    @Transactional(readOnly = true)
    public List<OrganizationInvitationResponse> getPendingInvitations(UUID organizationId, UUID userId) {
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        OrganizationMember member = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this organization"));

        if (member.getRole() != OrganizationRole.OWNER && member.getRole() != OrganizationRole.ADMIN) {
            throw new AccessDeniedException("You don't have permission to view invitations");
        }

        return organizationMemberRepository
                .findAllByOrganizationIdAndStatusWithUser(organizationId, MemberStatus.INVITED)
                .stream()
                .map(OrganizationInvitationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyInvitationResponse> getMyInvitations(UUID userId) {
        return organizationMemberRepository
                .findAllByUserIdAndStatusWithOrganizationDetails(userId, MemberStatus.INVITED)
                .stream()
                .filter(member -> member.getInviteToken() != null)
                .map(MyInvitationResponse::from)
                .toList();
    }

    @Transactional
    public OrganizationInvitationResponse resendInvitationEmail(UUID organizationId, UUID inviteId, UUID userId) {
        OrganizationMember member = organizationMemberRepository.findByInviteToken(inviteId.toString())
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        OrganizationMember currentMember = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this organization"));

        if (currentMember.getRole() != OrganizationRole.OWNER && currentMember.getRole() != OrganizationRole.ADMIN) {
            throw new AccessDeniedException("You don't have permission to view invitations");
        }

        if (member.getStatus() != MemberStatus.INVITED ||
                member.getInviteExpiresAt() == null || member.getInviteExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invitation is not active");
        }

        String newToken = randomUUIDToken();
        member.setInviteToken(newToken);
        member.setInviteExpiresAt(LocalDateTime.now().plusHours(invitationExpiryHours));

        emailService.sendOrganizationInvitationEmail(
                member.getUser().getEmail(),
                member.getUser().getName(),
                currentMember.getUser().getEmail(),
                member.getOrganization().getName(),
                member.getRole().name(),
                member.getInviteToken()
        );

        return OrganizationInvitationResponse.from(member);
    }

    public void cancelInvitation(UUID organizationId, UUID inviteId, UUID userId) {
        OrganizationMember member = organizationMemberRepository.findByInviteToken(inviteId.toString())
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        OrganizationMember currentMember = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this organization"));

        if (currentMember.getRole() != OrganizationRole.OWNER && currentMember.getRole() != OrganizationRole.ADMIN) {
            throw new AccessDeniedException("You don't have permission to cancel invitations");
        }

        if (member.getStatus() != MemberStatus.INVITED ||
                member.getInviteExpiresAt() == null || member.getInviteExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invitation is not active");
        }

        member.setStatus(MemberStatus.REMOVED);
        member.setInviteToken(null);
        member.setInviteExpiresAt(null);
    }

    public void declineInvitation(String token, UUID userId) {
        OrganizationMember member = organizationMemberRepository.findByInviteToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!member.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to decline this invitation");
        }

        if (member.getStatus() != MemberStatus.INVITED ||
                member.getInviteExpiresAt() == null || member.getInviteExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invitation is not active");
        }

        member.setStatus(MemberStatus.REMOVED);
        member.setInviteToken(null);
        member.setInviteExpiresAt(null);
    }

    public void validateInvitationToken(String token) {
        OrganizationMember member = organizationMemberRepository.findByInviteToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (member.getStatus() != MemberStatus.INVITED ||
                member.getInviteExpiresAt() == null || member.getInviteExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invitation token is not valid");
        }
    }

    @Transactional
    public void transferOrganizationOwnership(
            UUID organizationId,
            @Valid TransferOrganizationOwnershipRequest request,
            UUID userID) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        OrganizationMember currentOwner = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userID)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this organization"));

        if (currentOwner.getRole() != OrganizationRole.OWNER) {
            throw new AccessDeniedException("Only the current owner can transfer ownership");
        }

        OrganizationMember newOwner = organizationMemberRepository
                .findByOrganizationIdAndUserId(organizationId, request.newOwnerUserId())
                .orElseThrow(() -> new ResourceNotFoundException("New owner must be a member of the organization"));

        currentOwner.setRole(OrganizationRole.ADMIN);
        newOwner.setRole(OrganizationRole.OWNER);

        emailService.sendOwnershipTransferredToNewOwnerEmail(
                newOwner.getUser().getEmail(),
                newOwner.getUser().getName(),
                organization.getName(),
                organization.getSlug(),
                currentOwner.getUser().getEmail(),
                currentOwner.getUser().getName()
        );

        emailService.sendOwnershipTransferredToPreviousOwnerEmail(
                currentOwner.getUser().getEmail(),
                currentOwner.getUser().getName(),
                organization.getName(),
                organization.getSlug(),
                newOwner.getUser().getEmail(),
                newOwner.getUser().getName()
        );
    }

    public BulkInviteMemberResponse bulkInviteMembers(
            UUID organizationId,
            @Valid BulkInviteMembersRequest request,
            UserPrincipal userPrincipal) {

        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        OrganizationMember currentMember = organizationMemberRepository
                .findByOrganizationIdAndUserId(organizationId, userPrincipal.getId())
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this organization"));

        if (currentMember.getRole() != OrganizationRole.OWNER && currentMember.getRole() != OrganizationRole.ADMIN) {
            throw new AccessDeniedException("You don't have permission to invite members");
        }

        List<BulkInviteMemberResponse.Result> results = request.invites().stream()
                .map(inviteMember -> {
                    try {
                        inviteUserToOrganization(
                                organizationId,
                                new InviteUserToOrganizationRequest(inviteMember.email(), inviteMember.role()),
                                userPrincipal
                        );
                        return new BulkInviteMemberResponse.Result(
                                inviteMember.email(),
                                inviteMember.role(),
                                true,
                                "Invitation sent successfully"
                        );
                    } catch (Exception e) {
                        log.error("Failed to invite user: {} with email: {}. Error: {}",
                                inviteMember.role(), inviteMember.email(), e.getMessage());
                        return new BulkInviteMemberResponse.Result(
                                inviteMember.email(),
                                inviteMember.role(),
                                false,
                                "Failed to send invitation: " + e.getMessage()
                        );
                    }
                })
                .toList();

        int total = results.size();
        int successCount = (int) results.stream().filter(BulkInviteMemberResponse.Result::success).count();
        int failureCount = total - successCount;

        return new BulkInviteMemberResponse(total, successCount, failureCount, results);
    }

    public OrganizationUsageMetricsResponse getOrganizationUsageMetrics(UUID organizationId, UUID userId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        OrganizationMember currentMember = organizationMemberRepository
                .findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this organization"));

        if (currentMember.getRole() != OrganizationRole.OWNER && currentMember.getRole() != OrganizationRole.ADMIN) {
            throw new AccessDeniedException("You don't have view usage metrics for this organization");
        }

        int totalMembers = organizationMemberRepository.countByOrganizationIdAndStatus(organizationId, MemberStatus.ACTIVE);
        int maxMembers = organization.getMaxMembers();
        int remainingMembers = maxMembers - totalMembers;
        int totalProjects = projectRepository.countByOrganizationId(organizationId);
        int maxProjects = organization.getMaxProjects();
        int remainingProjects = maxProjects - totalProjects;

        return new OrganizationUsageMetricsResponse(
                organizationId,
                totalMembers,
                maxMembers,
                remainingMembers,
                totalProjects,
                maxProjects,
                remainingProjects);
    }

    public void leaveOrganization(UUID organizationId, UUID userId) {
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        OrganizationMember currentMember = organizationMemberRepository
                .findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this organization"));

        if (currentMember.getRole() == OrganizationRole.OWNER) {
            long ownerCount = organizationMemberRepository.countByOrganizationIdAndRole(organizationId, OrganizationRole.OWNER);
            if (ownerCount <= 1) {
                throw new IllegalStateException("You cannot leave the organization as you are the only owner. " +
                        "Please transfer ownership to another member before leaving.");
            }
        }

        currentMember.setStatus(MemberStatus.REMOVED);
        organizationMemberRepository.save(currentMember);
    }
}
