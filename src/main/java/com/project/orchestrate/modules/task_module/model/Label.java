package com.project.orchestrate.modules.task_module.model;

import com.project.orchestrate.modules.organization_module.model.Organization;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "labels",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_label_org_name",
                columnNames = {"organization_id", "name"}
        )
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;                    // "bug", "feature", "urgent"

    @Column(nullable = false, length = 7)
    private String color;                   // "#EF4444"

    // Scoped to org — labels are reusable across projects within same org
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, updatable = false)
    private Organization organization;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
