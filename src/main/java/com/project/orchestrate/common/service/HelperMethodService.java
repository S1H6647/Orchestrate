package com.project.orchestrate.common.service;

import com.project.orchestrate.modules.project_module.repository.ProjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class HelperMethodService {

    private final ProjectRepository projectRepository;

    public static String randomUUIDToken() {
        return UUID.randomUUID().toString();
    }

    public String generateSlug(String name) {
        if (name == null) return "";

        return name
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")   // remove special chars
                .replaceAll("\\s+", "-")           // spaces → dash
                .replaceAll("-{2,}", "-");         // collapse multiple dashes
    }

    public String generateSlug(UUID organizationId, String name) {
        String base = name
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");

        String slug = base;
        int suffix = 2;
        while (projectRepository.existsByOrganizationIdAndSlug(organizationId, slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }
}
