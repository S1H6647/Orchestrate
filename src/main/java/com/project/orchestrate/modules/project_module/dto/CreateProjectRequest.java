package com.project.orchestrate.modules.project_module.dto;

import com.project.orchestrate.modules.project_module.model.enums.ProjectType;
import com.project.orchestrate.modules.project_module.model.enums.ProjectVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be a valid hex code")
        String color,

        @NotNull(message = "Project type is required")
        ProjectType type,

        @NotNull(message = "Visibility is required")
        ProjectVisibility visibility,

        LocalDate startDate,
        LocalDate targetDate
) {
}

