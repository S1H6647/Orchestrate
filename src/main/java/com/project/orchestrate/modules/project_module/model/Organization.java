//package com.project.orchestrate.modules.project_module.model;
//
//import com.project.orchestrate.modules.project_module.model.enums.Plan;
//import com.project.orchestrate.modules.user_module.model.OrganizationMember;
//import com.project.orchestrate.modules.user_module.model.User;
//import com.project.orchestrate.modules.user_module.model.enums.OrganizationStatus;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedDate;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import static jakarta.persistence.GenerationType.UUID;
//
//@Entity
//@Table(name = "organization")
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class Organization {
//    // ── Identity ──────────────────────────────────────────
//    @Id
//    @GeneratedValue(strategy = UUID)
//    private UUID id;
//
//    @Column(nullable = false, length = 100)
//    private String name;
//
//    @Column(nullable = false, unique = true, length = 100)
//    private String slug;                    // "acme-corp" — URL identifier, immutable after set
//
//    @Column(length = 500)
//    private String description;             // optional org bio/tagline
//
//    @Column(length = 255)
//    private String logoUrl;                 // Cloudinary URL, nullable
//
//    @Column(length = 255)
//    private String websiteUrl;              // optional company website
//
//    // ── Plan / Subscription ───────────────────────────────
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    private Plan plan = Plan.FREE;          // FREE, PRO, ENTERPRISE
//
//    @Column(nullable = false)
//    private int maxMembers = 5;             // enforced at invite time (plan-driven)
//
//    @Column(nullable = false)
//    private int maxProjects = 3;            // enforced at project creation (plan-driven)
//
//    // ── Status ────────────────────────────────────────────
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    private OrganizationStatus status = OrganizationStatus.ACTIVE;   // ACTIVE, SUSPENDED, DELETED
//
//    // ── Audit ─────────────────────────────────────────────
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "created_by", nullable = false, updatable = false)
//    private User createdBy;                 // immutable — who originally created the org
//
//    @CreatedDate
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @LastModifiedDate
//    @Column(nullable = false)
//    private LocalDateTime updatedAt;
//
//    @Column
//    private LocalDateTime deletedAt;        // soft delete timestamp
//
//    // ── Relationships ─────────────────────────────────────
//    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<OrganizationMember> members = new ArrayList<>();
//
//    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Project> projects = new ArrayList<>();
//
//}
