package com.bank.quota.core.domain;

import com.bank.quota.core.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quota_freeze", indexes = {
    @Index(name = "idx_freeze_no", columnList = "freeze_no"),
    @Index(name = "idx_quota_id", columnList = "quota_id"),
    @Index(name = "idx_quota_type", columnList = "quota_type"),
    @Index(name = "idx_freeze_type", columnList = "freeze_type"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_freeze_time", columnList = "freeze_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"quota"})
public class QuotaFreeze implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "freeze_no", nullable = false, unique = true, length = 50)
    private String freezeNo;

    @Column(name = "quota_id", nullable = false)
    private Long quotaId;

    @Column(name = "quota_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private QuotaType quotaType;

    @Column(name = "freeze_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FreezeType freezeType;

    @Column(name = "freeze_amount", precision = 24, scale = 6)
    private BigDecimal freezeAmount;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "condition", length = 500)
    private String condition;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FreezeStatus status;

    @Column(name = "operator", nullable = false, length = 50)
    private String operator;

    @Column(name = "freeze_time", nullable = false)
    private LocalDateTime freezeTime;

    @Column(name = "unfreeze_time")
    private LocalDateTime unfreezeTime;

    @Column(name = "unfreeze_amount", precision = 24, scale = 6)
    private BigDecimal unfreezeAmount;

    @Column(name = "unfreeze_reason", length = 500)
    private String unfreezeReason;

    @Column(name = "unfreeze_operator", length = 50)
    private String unfreezeOperator;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = FreezeStatus.FROZEN;
        }
        if (freezeTime == null) {
            freezeTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
