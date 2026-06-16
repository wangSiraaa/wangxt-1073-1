package com.seafood.scale.entity;

import com.seafood.scale.enums.ScaleStatus;
import com.seafood.scale.enums.SealStatus;
import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scale")
public class Scale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scale_code", unique = true, nullable = false, length = 50)
    private String scaleCode;

    @Column(name = "scale_model", length = 100)
    private String scaleModel;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "factory_number", length = 50)
    private String factoryNumber;

    @Column(name = "max_capacity", length = 20)
    private String maxCapacity;

    @Column(name = "accuracy", length = 20)
    private String accuracy;

    @Column(name = "stall_id")
    private Long stallId;

    @Column(name = "calibration_expire_date")
    private LocalDate calibrationExpireDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "seal_status", length = 20)
    private SealStatus sealStatus = SealStatus.INTACT;

    @Column(name = "seal_number", length = 50)
    private String sealNumber;

    @Column(name = "business_qualified")
    private Boolean businessQualified = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private ScaleStatus status = ScaleStatus.NORMAL;

    @Column(name = "is_borrowed")
    private Boolean isBorrowed = false;

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
