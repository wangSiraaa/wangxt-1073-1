package com.seafood.scale.entity;

import com.seafood.scale.enums.StallStatus;
import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stall")
public class Stall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stall_code", unique = true, nullable = false, length = 50)
    private String stallCode;

    @Column(name = "stall_name", length = 100)
    private String stallName;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "owner_name", length = 50)
    private String ownerName;

    @Column(name = "owner_phone", length = 20)
    private String ownerPhone;

    @Column(name = "business_license", length = 100)
    private String businessLicense;

    @Column(name = "business_qualified")
    private Boolean businessQualified = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StallStatus status = StallStatus.OPEN;

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
