package com.seafood.scale.entity;

import com.seafood.scale.enums.CalibrationResult;
import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "calibration")
public class Calibration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scale_id", nullable = false)
    private Long scaleId;

    @Column(name = "calibration_date", nullable = false)
    private LocalDate calibrationDate;

    @Column(name = "metrologist_id")
    private Long metrologistId;

    @Column(name = "metrologist_name", length = 50)
    private String metrologistName;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 30, nullable = false)
    private CalibrationResult result;

    @Column(name = "next_calibration_date")
    private LocalDate nextCalibrationDate;

    @Column(name = "error_value", length = 20)
    private String errorValue;

    @Column(name = "temperature", length = 20)
    private String temperature;

    @Column(name = "humidity", length = 20)
    private String humidity;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "rectification_deadline")
    private LocalDate rectificationDeadline;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
