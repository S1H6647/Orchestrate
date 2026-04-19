package com.project.orchestrate.modules.project_module.dto;

import com.project.orchestrate.modules.project_module.model.enums.ProjectStatus;
import com.project.orchestrate.modules.project_module.model.enums.ProjectVisibility;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateProjectRequest(
        @Size(min = 2, max = 100)
        String name,

        @Size(max = 1000)
        String description,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6})$")
        String color,

        String coverImageUrl,
        ProjectVisibility visibility,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate targetDate,
        UUID leadId
) {
}

