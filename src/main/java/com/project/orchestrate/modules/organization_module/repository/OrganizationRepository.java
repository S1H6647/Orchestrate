package com.project.orchestrate.modules.organization_module.repository;

import com.project.orchestrate.modules.organization_module.model.Organization;
import com.project.orchestrate.modules.user_module.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    List<Organization> findByCreatedBy(User user);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID organizationId);

    // For critical operations like task creation where we need to ensure the taskSequence is updated atomically,
    // we can use a pessimistic lock to prevent
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Organization o WHERE o.id = :id")
    Optional<Organization> findByIdWithLock(@Param("id") UUID id);
}
