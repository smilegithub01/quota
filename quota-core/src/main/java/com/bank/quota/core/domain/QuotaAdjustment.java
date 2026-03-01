package com.bank.quota.core.domain;

import com.bank.quota.core.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quota_adjustment", indexes = {
    @Index(name = "idx_adjustment_no", columnList = "adjustment_no"),
    @Index(name = "idx_quota_id", columnList = "quota_id"),
    @Index(name = "idx_quota_type", columnList = "quota_type"),
    @Index(name = "idx_adjustment_type", columnList = "adjustment_type"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_create_time", columnList = "create_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"quota"})
public class QuotaAdjustment implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "adjustment_no", nullable = false, unique = true, length = 50)
    private String adjustmentNo;

    @Column(name = "quota_id", nullable = false)
    private Long quotaId;

    @Column(name = "quota_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    public QuotaType quotaType;

    @Column(name = "adjustment_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AdjustmentType adjustmentType;

    @Column(name = "adjustment_amount", nullable = false, precision = 24, scale = 6)
    private BigDecimal adjustmentAmount;

    @Column(name = "before_amount", nullable = false, precision = 24, scale = 6)
    private BigDecimal beforeAmount;

    @Column(name = "after_amount", nullable = false, precision = 24, scale = 6)
    private BigDecimal afterAmount;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "effective_time")
    private LocalDateTime effectiveTime;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AdjustmentStatus status;

    @Column(name = "applicant", nullable = false, length = 50)
    private String applicant;

    @Column(name = "approver", length = 50)
    private String approver;

    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = AdjustmentStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
