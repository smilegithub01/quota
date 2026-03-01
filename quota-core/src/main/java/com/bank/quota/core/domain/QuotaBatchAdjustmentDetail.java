package com.bank.quota.core.domain;

import com.bank.quota.core.enums.AdjustmentStatus;
import com.bank.quota.core.enums.AdjustmentType;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 额度批量调整明细实体
 * 
 * <p>代表批量额度调整中单个调整项的领域实体。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Entity
@Table(name = "quota_batch_adjustment_detail", indexes = {
    @Index(name = "idx_batch_id", columnList = "batch_id"),
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_create_time", columnList = "create_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaBatchAdjustmentDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "quota_type", length = 20)
    private String quotaType; // CUSTOMER, GROUP, ITEM等

    @Column(name = "adjustment_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AdjustmentType adjustmentType;

    @Column(name = "adjustment_amount", precision = 24, scale = 6)
    private BigDecimal adjustmentAmount;

    @Column(name = "before_amount", precision = 24, scale = 6)
    private BigDecimal beforeAmount;

    @Column(name = "after_amount", precision = 24, scale = 6)
    private BigDecimal afterAmount;

    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "CNY";

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AdjustmentStatus status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "process_time")
    private LocalDateTime processTime;

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
        if (currency == null) {
            currency = "CNY";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}