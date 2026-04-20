package com.project.orchestrate.modules.organization_module.repository;

import com.project.orchestrate.modules.organization_module.model.OrganizationMember;
import com.project.orchestrate.modules.organization_module.model.enums.OrganizationRole;
import com.project.orchestrate.modules.user_module.model.enums.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {

    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID orgId, UUID userId);

    @Query("""
            SELECT om
            FROM OrganizationMember om
            JOIN FETCH om.user
            WHERE om.inviteToken = :inviteToken
            """)
    Optional<OrganizationMember> findByInviteToken(@Param("inviteToken") String inviteToken);

    @Query("""
            SELECT om
            FROM OrganizationMember om
            JOIN FETCH om.user
            WHERE om.organization.id = :organizationId
              AND om.status = :status
            """)
    List<OrganizationMember> findAllByOrganizationIdAndStatusWithUser(
            @Param("organizationId") UUID organizationId,
            @Param("status") MemberStatus status
    );

    Optional<OrganizationMember> findByUserIdAndStatus(UUID id, MemberStatus memberStatus);

    @Query("""
            SELECT om
            FROM OrganizationMember om
            JOIN FETCH om.organization
            WHERE om.user.id = :userId
              AND om.status = :status
            """)
    List<OrganizationMember> findAllByUserIdAndStatusWithOrganization(
            @Param("userId") UUID userId,
            @Param("status") MemberStatus status
    );

    @Query("""
            SELECT om
            FROM OrganizationMember om
            JOIN FETCH om.organization
            WHERE om.user.id = :userId
              AND om.status = :status
              AND om.role = :role
            """)
    List<OrganizationMember> findAllByUserIdAndStatusAndRoleWithOrganization(
            @Param("userId") UUID userId,
            @Param("status") MemberStatus status,
            @Param("role") OrganizationRole role
    );

    @Query("""
            SELECT om
            FROM OrganizationMember om
            JOIN FETCH om.organization
            WHERE om.user.id = :userId
              AND om.status = :status
              AND om.role <> :role
            """)
    List<OrganizationMember> findAllByUserIdAndStatusAndRoleNotWithOrganization(
            @Param("userId") UUID userId,
            @Param("status") MemberStatus status,
            @Param("role") OrganizationRole role
    );

    @Query("""
            SELECT om
            FROM OrganizationMember om
            JOIN FETCH om.organization
            WHERE om.user.id = :userId
              AND om.status = :status
            """)
    List<OrganizationMember> findAllByUserIdAndStatusWithOrganizationDetails(
            @Param("userId") UUID userId,
            @Param("status") MemberStatus status
    );

    int countByOrganizationIdAndStatus(UUID organizationId, MemberStatus memberStatus);

    long countByOrganizationIdAndRole(UUID organizationId, OrganizationRole organizationRole);

    @Query(
            value = """
                    SELECT om
                    FROM OrganizationMember om
                    JOIN FETCH om.user u
                    WHERE om.organization.id = :organizationId
                      AND (:status IS NULL OR om.status = :status)
                      AND (:role IS NULL OR om.role = :role)
                      AND (
                          :query = ''
                          OR LOWER(u.name) LIKE CONCAT('%', :query, '%')
                          OR LOWER(u.email) LIKE CONCAT('%', :query, '%')
                      )
                    """,
            countQuery = """
                    SELECT COUNT(om)
                    FROM OrganizationMember om
                    JOIN om.user u
                    WHERE om.organization.id = :organizationId
                      AND (:status IS NULL OR om.status = :status)
                      AND (:role IS NULL OR om.role = :role)
                      AND (
                          :query = ''
                          OR LOWER(u.name) LIKE CONCAT('%', :query, '%')
                          OR LOWER(u.email) LIKE CONCAT('%', :query, '%')
                      )
                    """
    )
    Page<OrganizationMember> findMembersByFilters(
            @Param("organizationId") UUID organizationId,
            @Param("status") MemberStatus status,
            @Param("role") OrganizationRole role,
            @Param("query") String query,
            Pageable pageable
    );
}
