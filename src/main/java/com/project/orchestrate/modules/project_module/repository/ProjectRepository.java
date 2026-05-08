package com.project.orchestrate.modules.project_module.repository;

import com.project.orchestrate.modules.project_module.model.Project;
import com.project.orchestrate.modules.project_module.model.enums.ProjectStatus;
import com.project.orchestrate.modules.project_module.model.enums.ProjectVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("select p from Project p join fetch p.organization where p.id = :id")
    Optional<Project> findByIdWithOrganization(UUID id);

    List<Project> findAllByOrganizationId(UUID organizationId);

    List<Project> findByOrganizationIdAndStatus(UUID organizationId, ProjectStatus status);

    List<Project> findByOrganizationIdAndVisibility(UUID organizationId, ProjectVisibility visibility);

    boolean existsByOrganizationIdAndSlug(UUID organizationId, String slug);

    Optional<Project> findByOrganizationIdAndSlug(UUID organizationId, String slug);

    int countByOrganizationId(UUID organizationId);
}
