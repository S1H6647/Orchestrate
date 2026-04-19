package com.project.orchestrate.modules.project_module.model;

import com.project.orchestrate.modules.organization_module.model.Organization;
import com.project.orchestrate.modules.project_module.model.enums.ProjectStatus;
import com.project.orchestrate.modules.project_module.model.enums.ProjectType;
import com.project.orchestrate.modules.project_module.model.enums.ProjectVisibility;
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
@Table(name = "projects",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_project_org_slug",
                columnNames = {"organization_id", "slug"}
        )
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    // ── Identity ──────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String slug;                        // unique within org — "backend-api"

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, length = 7)
    private String color = "#6366F1";           // hex color for UI

    @Column(length = 255)
    private String coverImageUrl;

    // ── Type & Workflow ───────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectType type = ProjectType.BASIC;

    // ── Visibility ────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectVisibility visibility = ProjectVisibility.PUBLIC;

    // ── Status ────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.PLANNING;

    // ── Dates ─────────────────────────────────────────────
    @Column
    private LocalDate startDate;

    @Column
    private LocalDate targetDate;

    // ── Tenancy ───────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, updatable = false)
    private Organization organization;

    // ── Audit ─────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private User lead;                          // nullable, transferable

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime archivedAt;

    // ── Relationships ─────────────────────────────────────
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectMember> members = new ArrayList<>();

//    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Task> tasks = new ArrayList<>();
}
