package com.project.orchestrate.modules.task_module.repository;

import com.project.orchestrate.modules.task_module.model.Task;
import com.project.orchestrate.modules.task_module.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    // All top-level tasks in a project (no sub-tasks)
    List<Task> findAllByProjectIdAndParentTaskIsNullOrderByPosition(UUID projectId);

    // Sub-tasks of a parent
    List<Task> findAllByParentTaskIdOrderByPosition(UUID parentTaskId);

    // Filter by status — for kanban columns
    List<Task> findAllByProjectIdAndStatusOrderByPosition(UUID projectId, TaskStatus status);

    // Filter by assignee
    List<Task> findAllByProjectIdAndAssigneeId(UUID projectId, UUID assigneeId);

    List<Task> findAllByProjectIdAndStatusAndAssigneeIdOrderByPosition(UUID projectId, TaskStatus status, UUID assigneeId);


    // All tasks assigned to a user across all projects in an org
    @Query("""
            SELECT t FROM Task t
            JOIN t.project p
            WHERE p.organization.id = :organizationId
            AND t.assignee.id = :userId
            """)
    List<Task> findAllByOrganizationIdAndAssigneeId(
            @Param("organizationId") UUID organizationId,
            @Param("userId") UUID userId
    );

    // Count tasks per status — for project dashboard
    @Query("""
            SELECT t.status, COUNT(t)
            FROM Task t
            WHERE t.project.id = :projectId
            AND t.parentTask IS NULL
            GROUP BY t.status
            """)
    List<Object[]> countByStatusForProject(@Param("projectId") UUID projectId);

    @Query("SELECT COALESCE(MAX(t.position), 0.0) FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    Double findMaxPositionByProjectIdAndStatus(@Param("projectId") UUID projectId, @Param("status") TaskStatus status);
}
