package com.bank.quota.core.domain;

import com.bank.quota.core.enums.ApprovalStatus;
import com.bank.quota.core.enums.BusinessType;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 审批流程实体
 * 
 * <p>代表审批流程的领域实体。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Entity
@Table(name = "approval_process", indexes = {
    @Index(name = "idx_business_id", columnList = "business_id"),
    @Index(name = "idx_business_type", columnList = "business_type"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_create_time", columnList = "create_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalProcess implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_no", nullable = false, unique = true, length = 32)
    private String processNo;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "business_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Column(name = "business_name", length = 100)
    private String businessName;

    @Column(name = "applicant_id", nullable = false, length = 50)
    private String applicantId;

    @Column(name = "applicant_name", nullable = false, length = 100)
    private String applicantName;

    @Column(name = "amount", precision = 24, scale = 6)
    private BigDecimal amount;

    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "CNY";

    @Column(name = "current_node_id")
    private Long currentNodeId;

    @Column(name = "current_approver_id", length = 50)
    private String currentApproverId;

    @Column(name = "current_approver_name", length = 100)
    private String currentApproverName;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "estimated_duration_days")
    private Integer estimatedDurationDays;

    @Column(name = "description", length = 500)
    private String description;

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
            status = ApprovalStatus.DRAFT;
        }
        if (currency == null) {
            currency = "CNY";
        }
        if (processNo == null) {
            // 生成流程编号
            processNo = generateProcessNo();
        }
        startTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    private String generateProcessNo() {
        // 生成审批流程编号，格式：AP + 年月日 + 4位随机数
        String dateStr = LocalDateTime.now().toString().replaceAll("[^0-9]", "").substring(0, 8);
        String randomStr = String.format("%04d", (int)(Math.random() * 10000));
        return "AP" + dateStr + randomStr;
    }
}