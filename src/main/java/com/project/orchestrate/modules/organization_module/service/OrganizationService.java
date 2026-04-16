package com.project.orchestrate.modules.organization_module.service;

import com.project.orchestrate.common.exception.ResourceNotFoundException;
import com.project.orchestrate.common.service.StorageService;
import com.project.orchestrate.modules.organization_module.config.OrganizationPlanProperties;
import com.project.orchestrate.modules.organization_module.dto.CreateOrganizationRequest;
import com.project.orchestrate.modules.organization_module.dto.OrganizationMemberResponse;
import com.project.orchestrate.modules.organization_module.dto.OrganizationResponse;
import com.project.orchestrate.modules.organization_module.mapper.OrganizationMapper;
import com.project.orchestrate.modules.organization_module.model.Organization;
import com.project.orchestrate.modules.organization_module.model.OrganizationMember;
import com.project.orchestrate.modules.organization_module.model.enums.OrganizationRole;
import com.project.orchestrate.modules.organization_module.model.enums.OrganizationStatus;
import com.project.orchestrate.modules.organization_module.model.enums.Plan;
import com.project.orchestrate.modules.organization_module.repository.OrganizationMemberRepository;
import com.project.orchestrate.modules.organization_module.repository.OrganizationRepository;
import com.project.orchestrate.modules.user_module.model.User;
import com.project.orchestrate.modules.user_module.model.enums.MemberStatus;
import com.project.orchestrate.modules.user_module.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.project.orchestrate.common.service.HelperMethodService.toOrganizationSlug;

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

    @Transactional
    public OrganizationResponse createOrganization(@Valid CreateOrganizationRequest request, MultipartFile image, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String slug = request.slug();
        if (slug == null || slug.isBlank()) {
            slug = toOrganizationSlug(request.name());
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
                .createdBy(user)
                .maxMembers(limits.getMaxMembers())
                .maxProjects(limits.getMaxProjects())
                .plan(selectedPlan)
                .status(OrganizationStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OrganizationMember member = OrganizationMember.builder()
                .user(user)
                .organization(organization)
                .role(OrganizationRole.OWNER)
                .status(MemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        var saved = organizationRepository.save(organization);
        organizationMemberRepository.save(member);
        return organizationMapper.mapToOrganizationResponse(saved);
    }

    @Transactional
    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll()
                .stream()
                .map(organizationMapper::mapToOrganizationResponse)
                .toList();
    }

    @Transactional
    public OrganizationResponse getMyOrganization(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Organization organization = organizationRepository.findByCreatedBy(user)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        return organizationMapper.mapToOrganizationResponse(organization);
    }

    @Transactional
    public List<OrganizationMemberResponse> getOrganizationMembers(UUID organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        return organization.getMembers()
                .stream()
                .map(OrganizationMemberResponse::from)
                .toList();
    }

    @Transactional
    public void deleteOrganization(UUID organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        organizationRepository.delete(organization);
    }
}
