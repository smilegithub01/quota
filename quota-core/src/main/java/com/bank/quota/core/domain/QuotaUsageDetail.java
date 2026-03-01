package com.bank.quota.core.domain;

import com.bank.quota.core.enums.BusinessType;
import com.bank.quota.core.enums.QuotaUsageType;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 额度使用明细实体
 * 
 * <p>代表额度使用明细的领域实体。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Entity
@Table(name = "quota_usage_detail", indexes = {
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_group_id", columnList = "group_id"),
    @Index(name = "idx_business_type", columnList = "business_type"),
    @Index(name = "idx_usage_type", columnList = "usage_type"),
    @Index(name = "idx_usage_date", columnList = "usage_date"),
    @Index(name = "idx_create_time", columnList = "create_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaUsageDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usage_no", nullable = false, unique = true, length = 32)
    private String usageNo;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name", length = 100)
    private String groupName;

    @Column(name = "business_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Column(name = "usage_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private QuotaUsageType usageType; // OCCUPY, RELEASE, ADJUST, FREEZE, THAW, WRITE_OFF, REPAYMENT, EXPIRY

    @Column(name = "usage_amount", nullable = false, precision = 24, scale = 6)
    private BigDecimal usageAmount;

    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "CNY";

    @Column(name = "original_balance", precision = 24, scale = 6)
    private BigDecimal originalBalance;

    @Column(name = "current_balance", precision = 24, scale = 6)
    private BigDecimal currentBalance;

    @Column(name = "balance_after", precision = 24, scale = 6)
    private BigDecimal balanceAfter;

    @Column(name = "related_id", length = 50)
    private String relatedId; // 关联的申请ID、合同ID等

    @Column(name = "related_type", length = 50)
    private String relatedType; // APPLICATION, CONTRACT, LOAN等

    @Column(name = "usage_date")
    private LocalDateTime usageDate;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "operator_id", length = 50)
    private String operatorId;

    @Column(name = "operator_name", length = 100)
    private String operatorName;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "quota_id")
    private Long quotaId;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (currency == null) {
            currency = "CNY";
        }
        if (usageNo == null) {
            // 生成使用明细编号
            usageNo = generateUsageNo();
        }
        if (usageDate == null) {
            usageDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    private String generateUsageNo() {
        // 生成额度使用明细编号，格式：QUD + 年月日 + 6位随机数
        String dateStr = LocalDateTime.now().toString().replaceAll("[^0-9]", "").substring(0, 8);
        String randomStr = String.format("%06d", (int)(Math.random() * 1000000));
        return "QUD" + dateStr + randomStr;
    }
}