package com.seafood.scale.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rectification_reminder")
public class RectificationReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "calibration_id", nullable = false)
    private Long calibrationId;

    @Column(name = "scale_id", nullable = false)
    private Long scaleId;

    @Column(name = "stall_id")
    private Long stallId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 20, nullable = false)
    private ReminderTargetType targetType;

    @Column(name = "reminder_date", nullable = false)
    private LocalDate reminderDate;

    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    @Column(name = "is_sent")
    private Boolean isSent = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "content", length = 500)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum ReminderTargetType {
        STALL_OWNER,
        ADMIN
    }
}
