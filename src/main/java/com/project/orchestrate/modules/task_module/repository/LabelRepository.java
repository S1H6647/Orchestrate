package com.project.orchestrate.modules.task_module.repository;

import com.project.orchestrate.modules.task_module.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LabelRepository extends JpaRepository<Label, UUID> {
    List<Label> findAllByOrganizationId(UUID organizationId);

    boolean existsByOrganizationIdAndName(UUID organizationId, String name);

    Optional<Label> findByOrganizationIdAndId(UUID organizationId, UUID id);

    Optional<Label> findByOrganizationIdAndName(UUID organizationId, String name);
}
