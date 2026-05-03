package com.project.orchestrate.modules.task_module.model;

import com.project.orchestrate.modules.project_module.model.Project;
import com.project.orchestrate.modules.task_module.model.enums.TaskPriority;
import com.project.orchestrate.modules.task_module.model.enums.TaskStatus;
import com.project.orchestrate.modules.user_module.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Task {

    // ── Identity ──────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // Human-readable identifier within org e.g. "ACME-42"
    // Generated: orgSlug.toUpperCase() + "-" + sequenceNumber
    @Column(nullable = false, length = 20, updatable = false)
    private String identifier;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;             // rich text / markdown

    // ── Status & Priority ─────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.BACKLOG;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority = TaskPriority.NO_PRIORITY;

    // ── Ordering ──────────────────────────────────────────
    @Column(nullable = false)
    private Double position = 0.0;          // for drag-and-drop ordering within status column

    // ── Dates ─────────────────────────────────────────────
    @Column
    private LocalDate dueDate;

    @Column
    private LocalDateTime completedAt;      // set when status → DONE

    // ── SCRUM fields ──────────────────────────────────────
    @Column
    private Integer storyPoints;            // null for KANBAN/BASIC projects

    // ── Relationships — Core ──────────────────────────────

    // Which project this task belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;

    // Who is responsible for completing it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;                  // nullable — unassigned tasks are valid

    // Who created/reported this task
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false, updatable = false)
    private User reporter;

    // ── Sub-tasks (one level only) ────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;                // null = top-level task

    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> subTasks = new ArrayList<>();

    // ── Labels ────────────────────────────────────────────
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_labels",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    private List<Label> labels = new ArrayList<>();

    // ── Audit ─────────────────────────────────────────────
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
