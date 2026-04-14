package com.project.orchestrate.modules.project_module.service;

import com.project.orchestrate.common.exception.ResourceNotFoundException;
import com.project.orchestrate.common.service.StorageService;
import com.project.orchestrate.modules.project_module.dto.CreateOrganizationRequest;
import com.project.orchestrate.modules.project_module.dto.OrganizationResponse;
import com.project.orchestrate.modules.project_module.mapper.OrganizationMapper;
import com.project.orchestrate.modules.project_module.model.Organization;
import com.project.orchestrate.modules.project_module.model.enums.Plan;
import com.project.orchestrate.modules.project_module.repository.OrganizationRepository;
import com.project.orchestrate.modules.user_module.model.User;
import com.project.orchestrate.modules.user_module.model.enums.OrganizationStatus;
import com.project.orchestrate.modules.user_module.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final OrganizationMapper organizationMapper;
    private final StorageService storageService;

    @Transactional
    public OrganizationResponse createOrganization(@Valid CreateOrganizationRequest request, MultipartFile image, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String slug = request.slug();
        if (slug == null || slug.isBlank()) {
            slug = toSlug(request.name());
        }

        String logoUrl = storageService.upload(image);

        Organization organization = Organization.builder()
                .name(request.name())
                .slug(slug)
                .description(request.description())
                .logoUrl(logoUrl)
                .websiteUrl(request.websiteUrl())
                .createdBy(user)
                .maxMembers(5)
                .maxProjects(3)
                .plan(Plan.FREE)
                .status(OrganizationStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        var saved = organizationRepository.save(organization);
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

    private String toSlug(String input) {
        if (input == null) return "";

        return input
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")   // remove special chars
                .replaceAll("\\s+", "-")           // spaces → dash
                .replaceAll("-{2,}", "-");         // collapse multiple dashes
    }
}
