package com.seafood.scale.entity;

import com.seafood.scale.enums.SuspensionType;
import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stall_suspension")
public class StallSuspension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stall_id", nullable = false)
    private Long stallId;

    @Enumerated(EnumType.STRING)
    @Column(name = "suspension_type", length = 30)
    private SuspensionType suspensionType;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "related_type", length = 20)
    private String relatedType;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", length = 50)
    private String operatorName;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "reopened_by_id")
    private Long reopenedById;

    @Column(name = "reopened_by_name", length = 50)
    private String reopenedByName;

    @Column(name = "reopen_reason", length = 500)
    private String reopenReason;

    @Column(name = "reopened_at")
    private LocalDateTime reopenedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        suspendedAt = LocalDateTime.now();
    }
}
