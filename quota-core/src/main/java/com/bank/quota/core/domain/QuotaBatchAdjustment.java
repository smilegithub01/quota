package com.bank.quota.core.domain;

import com.bank.quota.core.enums.AdjustmentStatus;
import com.bank.quota.core.enums.AdjustmentType;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 额度批量调整实体
 * 
 * <p>代表批量额度调整请求的领域实体。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Entity
@Table(name = "quota_batch_adjustment", indexes = {
    @Index(name = "idx_batch_no", columnList = "batch_no"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_create_time", columnList = "create_time")
})
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class QuotaBatchAdjustment implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_no", nullable = false, unique = true, length = 32)
    private String batchNo;

    @Column(name = "batch_name", nullable = false, length = 100)
    private String batchName;

    @Column(name = "adjustment_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AdjustmentType adjustmentType;

    @Column(name = "total_amount", precision = 24, scale = 6)
    private BigDecimal totalAmount;

    @Column(name = "item_count")
    private Integer itemCount;

    @Column(name = "processed_count")
    private Integer processedCount;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "failed_count")
    private Integer failedCount;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AdjustmentStatus status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "executor_id", length = 50)
    private String executorId;

    @Column(name = "executor_name", length = 100)
    private String executorName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "error_log", length = 2000)
    private String errorLog;

    @Column(name = "create_by", length = 50)
    private String createBy;

    @Column(name = "update_by", length = 50)
    private String updateBy;

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
        if (processedCount == null) {
            processedCount = 0;
        }
        if (successCount == null) {
            successCount = 0;
        }
        if (failedCount == null) {
            failedCount = 0;
        }
        if (batchNo == null) {
            batchNo = generateBatchNo();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    private String generateBatchNo() {
        // 生成批量调整编号，格式：QBA + 年月日 + 6位随机数
        String dateStr = LocalDateTime.now().toString().replaceAll("[^0-9]", "").substring(0, 8);
        String randomStr = String.format("%06d", (int)(Math.random() * 1000000));
        return "QBA" + dateStr + randomStr;
    }
}