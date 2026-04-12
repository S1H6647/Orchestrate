package com.project.orchestrate.modules.user_module.model;

//import com.project.orchestrate.modules.project_module.model.Organization;

import com.project.orchestrate.modules.user_module.model.enums.MemberStatus;
import com.project.orchestrate.modules.user_module.model.enums.OrganizationRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "organization_members")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationMember {
    // ── Identity ──────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;                        // unique identifier for this membership

    // ── Relationships ─────────────────────────────────────
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "organization_id", nullable = false)
//    private Organization organization;      // the organization this member belongs to (required)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;                      // the user who is a member (required)

    // ── Role & Status ─────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrganizationRole role;          // OWNER, ADMIN, MEMBER, VIEWER — determines permissions

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;            // ACTIVE, INVITED, REMOVED — tracks membership state

    // ── Invitation Workflow ───────────────────────────────
    @Column(length = 64)
    private String inviteToken;             // secure token for email-based invite acceptance (nullable if member was directly added)

    private LocalDateTime inviteExpiresAt;  // expiration time for the invite token (if not yet accepted)

    // ── Audit ─────────────────────────────────────────────
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;         // when the user joined the organization
}
