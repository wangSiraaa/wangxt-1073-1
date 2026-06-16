package com.seafood.scale.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scale_borrow")
public class ScaleBorrow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scale_id", nullable = false)
    private Long scaleId;

    @Column(name = "borrowed_to_stall_id")
    private Long borrowedToStallId;

    @Column(name = "original_scale_id")
    private Long originalScaleId;

    @Column(name = "borrow_context", length = 30)
    private String borrowContext;

    @Column(name = "borrower_id")
    private Long borrowerId;

    @Column(name = "borrower_name", length = 50)
    private String borrowerName;

    @Column(name = "borrow_reason", length = 200)
    private String borrowReason;

    @Column(name = "borrowed_at")
    private LocalDateTime borrowedAt;

    @Column(name = "expected_return_at")
    private LocalDateTime expectedReturnAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "is_returned")
    private Boolean isReturned = false;

    @Column(name = "return_remark", length = 500)
    private String returnRemark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        borrowedAt = LocalDateTime.now();
    }
}
