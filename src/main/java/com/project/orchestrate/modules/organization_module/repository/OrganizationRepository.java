package com.project.orchestrate.modules.project_module.repository;

import com.project.orchestrate.modules.project_module.model.Organization;
import com.project.orchestrate.modules.project_module.model.OrganizationMember;
import com.project.orchestrate.modules.user_module.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findByCreatedBy(User user);

    List<OrganizationMember> findByOrganizationMembers();
}
