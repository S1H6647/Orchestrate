package com.project.orchestrate.modules.task_module.service;

import com.project.orchestrate.common.exception.AccessDeniedException;
import com.project.orchestrate.common.exception.ResourceNotFoundException;
import com.project.orchestrate.modules.organization_module.model.Organization;
import com.project.orchestrate.modules.organization_module.model.OrganizationMember;
import com.project.orchestrate.modules.organization_module.model.enums.OrganizationRole;
import com.project.orchestrate.modules.organization_module.repository.OrganizationMemberRepository;
import com.project.orchestrate.modules.organization_module.repository.OrganizationRepository;
import com.project.orchestrate.modules.project_module.model.Project;
import com.project.orchestrate.modules.project_module.model.ProjectMember;
import com.project.orchestrate.modules.project_module.model.enums.ProjectRole;
import com.project.orchestrate.modules.project_module.model.enums.ProjectVisibility;
import com.project.orchestrate.modules.project_module.repository.ProjectMemberRepository;
import com.project.orchestrate.modules.project_module.repository.ProjectRepository;
import com.project.orchestrate.modules.redis_module.RedisPublisher;
import com.project.orchestrate.modules.task_module.dto.*;
import com.project.orchestrate.modules.task_module.model.Label;
import com.project.orchestrate.modules.task_module.model.Task;
import com.project.orchestrate.modules.task_module.model.enums.TaskStatus;
import com.project.orchestrate.modules.task_module.repository.LabelRepository;
import com.project.orchestrate.modules.task_module.repository.TaskRepository;
import com.project.orchestrate.modules.user_module.model.User;
import com.project.orchestrate.modules.user_module.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private static final String DEFAULT_LABEL_COLOR = "#EF4444";

    private final TaskRepository taskRepository;
    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final LabelRepository labelRepository;
    private final UserRepository userRepository;
    private final RedisPublisher redisPublisher;

    @Transactional
    public TaskResponse createTask(
            UUID organizationId,
            String projectSlug,
            @Valid CreateTaskRequest request,
            User currentUser
    ) throws BadRequestException {
        assertOrganizationMember(currentUser.getId(), organizationId);
        Project project = getProjectOrThrow(organizationId, projectSlug);
        assertCanContribute(currentUser, project);

        Task task = Task.builder()
                .identifier(generateIdentifier(project.getOrganization()))
                .title(request.title())
                .description(request.description())
                .status(request.status())
                .priority(request.priority())
                .dueDate(request.dueDate())
                .storyPoints(request.storyPoints())
                .project(project)
                .reporter(currentUser)
                .parentTask(request.parentTaskId() != null ? getTaskOrThrow(request.parentTaskId(), project.getId()) : null)
                .subTasks(new ArrayList<>())
                .labels(resolveLabels(request.labelIds(), organizationId))
                .build();

        // Max position
        Double maxPosition = taskRepository.findMaxPositionByProjectIdAndStatus(project.getId(), task.getStatus());

        task.setPosition(maxPosition + 1000.0);

        // Assignee
        if (request.assigneeId() != null) {
            User user = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            assertOrganizationMember(user.getId(), organizationId);
            task.setAssignee(user);
        }

        // Find parentTask
        if (request.parentTaskId() != null) {
            Task parentTask = taskRepository.findById(request.parentTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

            if (!parentTask.getProject().getId().equals(project.getId())) {
                throw new BadRequestException("Parent task must belong to the same project");
            }

            if (parentTask.getParentTask() != null) {
                throw new BadRequestException("Sub-task can not have sub-task");
            }
            task.setParentTask(parentTask);
        }

        // Find labels
        if (request.labelIds() != null && !request.labelIds().isEmpty()) {
            List<Label> labels = resolveLabels(request.labelIds(), organizationId);
            task.setLabels(labels);
        }

        taskRepository.save(task);

        log.info("Task created: {} in Project: {}", task.getIdentifier(), project.getSlug());

        redisPublisher.publishTaskCreatedEvent(
                currentUser.getId(),
                currentUser.getName(),
                project.getId(),
                TaskResponse.from(task)
        );

        return TaskResponse.from(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks(
            UUID organizationId,
            String projectSlug,
            TaskStatus status,
            UUID assigneeId,
            User currentUser
    ) {
        Project project = getProjectOrThrow(organizationId, projectSlug);
        assertCanAccess(currentUser, project);

        if (status != null) {
            if (assigneeId != null) {
                // Both filters at DB level
                return taskRepository
                        .findAllByProjectIdAndStatusAndAssigneeIdOrderByPosition(project.getId(), status, assigneeId)
                        .stream()
                        .filter(t -> t.getParentTask() == null)
                        .map(TaskResponse::from)
                        .toList();
            } else {
                // Status only, no assignee filter
                return taskRepository
                        .findAllByProjectIdAndStatusOrderByPosition(project.getId(), status)
                        .stream()
                        .filter(t -> t.getParentTask() == null)
                        .map(TaskResponse::from)
                        .toList();
            }
        }

        return taskRepository
                .findAllByProjectIdAndParentTaskIsNullOrderByPosition(project.getId())
                .stream()
                .filter(t -> assigneeId == null
                        || (t.getAssignee() != null
                        && t.getAssignee().getId().equals(assigneeId)))
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(
            UUID organizationId,
            String projectSlug,
            UUID taskId,
            User currentUser
    ) {
        Project project = getProjectOrThrow(organizationId, projectSlug);
        assertCanAccess(currentUser, project);

        Task task = getTaskOrThrow(taskId, project.getId());
        return TaskResponse.from(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getSubTasks(
            UUID organizationId,
            String projectSlug,
            UUID parentTaskId,
            User currentUser
    ) {
        Project project = getProjectOrThrow(organizationId, projectSlug);
        assertCanAccess(currentUser, project);

        return taskRepository.findAllByParentTaskIdOrderByPosition(parentTaskId)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional
    public TaskResponse updateTask(
            UUID organizationId,
            String projectSlug,
            UUID taskId,
            @Valid UpdateTaskRequest request,
            User user
    ) {
        assertOrganizationMember(user.getId(), organizationId);
        Project project = getProjectOrThrow(organizationId, projectSlug);
        assertCanAccess(user, project);

        Task task = getTaskOrThrow(taskId, project.getId());

        if (request.title() != null) task.setTitle(request.title());
        if (request.description() != null) task.setDescription(request.description());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());
        if (request.storyPoints() != null) task.setStoryPoints(request.storyPoints());

        // Assignee change
        if (request.assigneeId() != null) {
            var assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            assertOrganizationMember(assignee.getId(), organizationId);
            task.setAssignee(assignee);
        }

        // Replace labels
        if (request.labelIds() != null) {
            task.setLabels(resolveLabels(request.labelIds(), organizationId));
        }

        taskRepository.save(task);
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse reorderTask(UUID organizationId, String projectSlug, UUID taskId, @Valid ReorderTaskRequest request, User user) {
        assertOrganizationMember(user.getId(), organizationId);
        Project project = getProjectOrThrow(organizationId, projectSlug);
        assertCanAccess(user, project);

        Task task = getTaskOrThrow(taskId, project.getId());
        TaskStatus previousStatus = task.getStatus();

        task.setPosition(request.position());

        if (request.status() != task.getStatus()) {
            task.setStatus(request.status());
            if (request.status() == TaskStatus.DONE) {
                task.setCompletedAt(LocalDateTime.now());
            } else {
                task.setCompletedAt(null);
            }
        }
        Task saved = taskRepository.save(task);

        log.info("Task reordered: {} status {} -> {} position {}", saved.getId(), previousStatus, saved.getStatus(), saved.getPosition());

        redisPublisher.publishTaskMoveEvent(
                user.getId(),
                user.getName(),
                saved.getId(),
                project.getId(),
                previousStatus,
                saved.getStatus(),
                saved.getPosition()
        );

        return TaskResponse.from(saved);
    }

    public void deleteTask(UUID organizationId, String projectSlug, UUID taskId, User user) {
        assertOrganizationMember(user.getId(), organizationId);
        Project project = getProjectOrThrow(organizationId, projectSlug);
        assertCanAccess(user, project);

        Task task = getTaskOrThrow(taskId, project.getId());

        taskRepository.delete(task);
    }

    @Transactional
    public void addLabel(UUID organizationId, String projectSlug, UUID taskId, User user, AddLabelRequest request) {
        assertOrganizationMember(user.getId(), organizationId);
        Project project = getProjectOrThrow(organizationId, projectSlug);
        assertCanAccess(user, project);

        Task task = getTaskOrThrow(taskId, project.getId());

        String labelName = request.name();
        if (labelName == null || labelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Label name is required");
        }

        String normalizedName = labelName.trim();
        Label label = labelRepository.findByOrganizationIdAndName(organizationId, normalizedName)
                .orElseGet(() -> {
                    Label created = new Label();
                    created.setName(normalizedName);
                    created.setColor(DEFAULT_LABEL_COLOR);
                    created.setOrganization(project.getOrganization());
                    return labelRepository.save(created);
                });

        if (task.getLabels().stream().noneMatch(existing -> existing.getId().equals(label.getId()))) {
            task.getLabels().add(label);
        }

        taskRepository.save(task);
    }

    @Transactional
    public void removeLabel(UUID organizationId, String projectSlug, UUID taskId, User user, UUID labelId) {
        assertOrganizationMember(user.getId(), organizationId);
        Project project = getProjectOrThrow(organizationId, projectSlug);
        assertCanAccess(user, project);

        Task task = getTaskOrThrow(taskId, project.getId());

        boolean removed = task.getLabels().removeIf(label -> label.getId().equals(labelId));
        if (!removed) {
            throw new ResourceNotFoundException("Label not found on task");
        }

        taskRepository.save(task);
    }

    // ── Helpers ────────────────────────────────────────────

    private User getUserOrThrow(UUID organizationId, UUID userId) {
        OrganizationMember orgMember = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        return orgMember.getUser();
    }

    private Project getProjectOrThrow(UUID organizationId, String projectSlug) {
        return projectRepository.findByOrganizationIdAndSlug(organizationId, projectSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private Task getTaskOrThrow(UUID taskId, UUID projectId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Ensure task belongs to the requested project — prevents cross-project access
        if (!task.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Task not found");
        }

        return task;
    }

    private List<Label> resolveLabels(List<UUID> labelIds, UUID organizationId) {
        if (labelIds == null || labelIds.isEmpty()) {
            return new ArrayList<>();
        }

        return labelIds.stream()
                .map(id -> labelRepository.findByOrganizationIdAndId(organizationId, id)
                        .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + id)))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private void assertOrganizationMember(UUID userId, UUID organizationId) {
        if (!organizationMemberRepository.existsByOrganizationIdAndUserId(organizationId, userId)) {
            throw new AccessDeniedException("User is not a member of this organization");
        }
    }

    private void assertCanContribute(User user, Project project) {
        UUID orgId = project.getOrganization().getId();
        OrganizationRole orgRole = organizationMemberRepository
                .findByOrganizationIdAndUserId(orgId, user.getId())
                .map(OrganizationMember::getRole)
                .orElseThrow(() -> new AccessDeniedException("Not an org member"));

        if (orgRole == OrganizationRole.OWNER || orgRole == OrganizationRole.ADMIN) return;

        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(project.getId(), user.getId())
                .orElseThrow(() -> new AccessDeniedException("Not a project member"));

        // VIEWERs cannot create/edit tasks
        if (member.getRole() == ProjectRole.VIEWER) {
            throw new AccessDeniedException("Viewers cannot modify tasks");
        }
    }

    private void assertCanAccess(User user, Project project) {
        UUID orgId = project.getOrganization().getId();
        OrganizationRole orgRole = organizationMemberRepository
                .findByOrganizationIdAndUserId(orgId, user.getId())
                .map(OrganizationMember::getRole)
                .orElseThrow(() -> new AccessDeniedException("Not an organization member"));

        if (orgRole == OrganizationRole.OWNER || orgRole == OrganizationRole.ADMIN) return;
        if (project.getVisibility() == ProjectVisibility.PUBLIC) return;

        if (!projectMemberRepository.existsByProjectIdAndUserId(
                project.getId(), user.getId())
        ) {
            throw new AccessDeniedException("You do not have access to this project");
        }
    }

    @Transactional
    private String generateIdentifier(Organization org) {
        // Pessimistic lock — prevents two tasks getting the same number
        Organization locked = organizationRepository.findByIdWithLock(org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        locked.setTaskSequence(locked.getTaskSequence() + 1);
        organizationRepository.save(locked);

        String prefix = locked.getSlug()
                .toUpperCase()
                .replace("-", "")
                .substring(0, Math.min(7, locked.getSlug().length()));

        return prefix + "-" + locked.getTaskSequence();
    }
}
