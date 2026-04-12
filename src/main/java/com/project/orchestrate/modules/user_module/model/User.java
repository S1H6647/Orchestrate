package com.project.orchestrate.modules.user_module.model;

import com.project.orchestrate.modules.user_module.model.enums.AccountStatus;
import com.project.orchestrate.modules.user_module.model.enums.SystemRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    // ── Identity ──────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;                        // unique identifier

    // ── Profile ───────────────────────────────────────────
    @Column(name = "name", nullable = false, length = 100)
    private String name;                    // user's full name (display name)

    @Column(unique = true, nullable = false, length = 100)
    private String email;                   // unique email — used for login and notifications

    @Column(nullable = false)
    private String password;                // hashed password (bcrypt or similar)

    @Column(length = 255)
    private String avatarUrl;               // optional profile picture URL (Cloudinary or CDN)

    @Column(length = 20)
    private String phone;                   // optional phone number

    // ── System-level Role ─────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SystemRole systemRole = SystemRole.USER;         // SYSTEM_ADMIN or USER

    // ── Account Status ────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;           // ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION

    @Column(nullable = false)
    private boolean emailVerified = false;  // tracks whether email address has been verified

    // ── Refresh Token ─────────────────────────────────────
    @Column(length = 512)
    private String refreshToken;           // hashed, for token rotation

    private LocalDateTime refreshTokenExpiresAt;

    // ── Audit ─────────────────────────────────────────────
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;        // account creation timestamp

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;        // last profile update timestamp

    @Column
    private LocalDateTime lastLoginAt;

    // ── Relationships ─────────────────────────────────────
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrganizationMember> memberships = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
