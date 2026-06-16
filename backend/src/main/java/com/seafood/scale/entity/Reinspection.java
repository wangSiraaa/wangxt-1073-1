package com.seafood.scale.entity;

import com.seafood.scale.enums.ReinspectionStatus;
import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reinspection")
public class Reinspection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reinspection_no", unique = true, length = 50)
    private String reinspectionNo;

    @Column(name = "complaint_id")
    private Long complaintId;

    @Column(name = "stall_id")
    private Long stallId;

    @Column(name = "scale_id")
    private Long scaleId;

    @Column(name = "metrologist_id")
    private Long metrologistId;

    @Column(name = "metrologist_name", length = 50)
    private String metrologistName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private ReinspectionStatus status = ReinspectionStatus.PENDING;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "actual_time")
    private LocalDateTime actualTime;

    @Column(name = "standard_weight", precision = 10, scale = 3)
    private BigDecimal standardWeight;

    @Column(name = "measured_weight", precision = 10, scale = 3)
    private BigDecimal measuredWeight;

    @Column(name = "error_percentage", precision = 10, scale = 4)
    private BigDecimal errorPercentage;

    @Column(name = "is_borrowed_scale_used")
    private Boolean isBorrowedScaleUsed = false;

    @Column(name = "borrowed_scale_id")
    private Long borrowedScaleId;

    @Column(name = "result_remark", length = 500)
    private String resultRemark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
