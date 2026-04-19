package com.project.orchestrate.modules.organization_module.repository;

import com.project.orchestrate.modules.organization_module.model.Organization;
import com.project.orchestrate.modules.user_module.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    List<Organization> findByCreatedBy(User user);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID organizationId);

//    List<OrganizationMember> findAllByMembers();
}
