package com.project.orchestrate.modules.project_module.repository;

import com.project.orchestrate.modules.project_module.model.Organization;
import com.project.orchestrate.modules.user_module.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByCreatedBy(User user);
}
