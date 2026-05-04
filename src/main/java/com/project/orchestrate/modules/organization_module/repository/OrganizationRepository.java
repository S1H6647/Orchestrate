package com.project.orchestrate.modules.organization_module.repository;

import com.project.orchestrate.modules.organization_module.model.Organization;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    boolean existsBySlugAndIdNot(String slug, UUID organizationId);

    Optional<Organization> findBySlug(String slug);

    // For critical operations like task creation where we need to ensure the taskSequence is updated atomically,
    // we can use a pessimistic lock to prevent
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Organization o WHERE o.id = :id")
    Optional<Organization> findByIdWithLock(@Param("id") UUID id);

    @Query("""
                SELECT o
                FROM Organization o
                WHERE (
                    :q IS NULL
                    OR LOWER(o.name) LIKE LOWER(CONCAT('%', :q, '%'))
                )
            """)
    Page<Organization> searchOrganizations(
            @Param("q") String q,
            Pageable pageable
    );
}
