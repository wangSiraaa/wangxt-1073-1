package com.seafood.scale.entity;

import com.seafood.scale.enums.ComplaintStatus;
import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "complaint")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_no", unique = true, length = 50)
    private String complaintNo;

    @Column(name = "complainant_name", length = 50)
    private String complainantName;

    @Column(name = "complainant_phone", length = 20)
    private String complainantPhone;

    @Column(name = "stall_id")
    private Long stallId;

    @Column(name = "scale_id")
    private Long scaleId;

    @Column(name = "transaction_time")
    private LocalDateTime transactionTime;

    @Column(name = "goods_name", length = 100)
    private String goodsName;

    @Column(name = "display_weight", precision = 10, scale = 3)
    private BigDecimal displayWeight;

    @Column(name = "actual_weight", precision = 10, scale = 3)
    private BigDecimal actualWeight;

    @Column(name = "price_per_unit", precision = 10, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(name = "display_amount", precision = 10, scale = 2)
    private BigDecimal displayAmount;

    @Column(name = "actual_amount", precision = 10, scale = 2)
    private BigDecimal actualAmount;

    @Column(name = "overcharged_amount", precision = 10, scale = 2)
    private BigDecimal overchargedAmount;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private ComplaintStatus status = ComplaintStatus.PENDING;

    @Column(name = "handler_id")
    private Long handlerId;

    @Column(name = "handler_name", length = 50)
    private String handlerName;

    @Column(name = "handle_result", length = 500)
    private String handleResult;

    @Column(name = "handle_time")
    private LocalDateTime handleTime;

    @Column(name = "reinspection_id")
    private Long reinspectionId;

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
