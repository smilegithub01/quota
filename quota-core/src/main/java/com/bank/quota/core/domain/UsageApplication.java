package com.bank.quota.core.domain;

import com.bank.quota.core.enums.BusinessType;
import com.bank.quota.core.enums.UsageStatus;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用信申请实体
 * 
 * <p>代表客户用信申请的领域实体。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Entity
@Table(name = "usage_application", indexes = {
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_application_no", columnList = "application_no"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_create_time", columnList = "create_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageApplication implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_no", nullable = false, unique = true, length = 32)
    private String applicationNo;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "business_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Column(name = "usage_quota", nullable = false, precision = 24, scale = 6)
    private BigDecimal usageQuota;

    @Column(name = "used_quota", precision = 24, scale = 6)
    @Builder.Default
    private BigDecimal usedQuota = BigDecimal.ZERO;

    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "CNY";

    @Column(name = "guarantee_type", length = 50)
    private String guaranteeType;

    @Column(name = "collateral_value", precision = 24, scale = 6)
    private BigDecimal collateralValue;

    @Column(name = "term_months")
    private Integer termMonths;

    @Column(name = "interest_rate", precision = 10, scale = 6)
    private BigDecimal interestRate;

    @Column(name = "purpose", length = 500)
    private String purpose;

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UsageStatus status;

    @Column(name = "review_comments", length = 1000)
    private String reviewComments;

    @Column(name = "approver_id")
    private String approverId;

    @Column(name = "approver_name", length = 100)
    private String approverName;

    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "contract_id", length = 50)
    private String contractId;

    @Column(name = "related_approval_id")
    private Long relatedApprovalId;

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
            status = UsageStatus.DRAFT;
        }
        if (currency == null) {
            currency = "CNY";
        }
        if (usedQuota == null) {
            usedQuota = BigDecimal.ZERO;
        }
        if (applicationNo == null) {
            // 生成申请编号
            applicationNo = generateApplicationNo();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    private String generateApplicationNo() {
        // 生成用信申请编号，格式：UA + 年月日 + 4位随机数
        String dateStr = LocalDateTime.now().toString().replaceAll("[^0-9]", "").substring(0, 8);
        String randomStr = String.format("%04d", (int)(Math.random() * 10000));
        return "UA" + dateStr + randomStr;
    }
}