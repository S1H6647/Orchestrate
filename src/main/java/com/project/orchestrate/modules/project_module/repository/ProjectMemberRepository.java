package com.project.orchestrate.modules.project_module.repository;

import com.project.orchestrate.modules.project_module.model.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);

    List<ProjectMember> findAllByProjectId(UUID projectId);

    @Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.user WHERE pm.project.id = :projectId")
    List<ProjectMember> findAllByProjectIdWithUser(UUID projectId);

    @Modifying
    @Query("delete from ProjectMember pm where pm.project.id = :projectId and pm.user.id = :userId")
    void deleteByProjectIdAndUserId(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}
