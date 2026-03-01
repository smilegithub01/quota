package com.bank.quota.core.domain;

import com.bank.quota.core.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_quota", indexes = {
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_group_id", columnList = "group_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_customer_type", columnList = "customer_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"customer", "groupQuota"})
public class CustomerQuota implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private Long customerId;

    @Column(name = "customer_no", length = 32)
    private String customerNo;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CustomerType customerType;

    @Column(name = "category", length = 20)
    @Enumerated(EnumType.STRING)
    private CustomerCategory category;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "total_quota", nullable = false, precision = 24, scale = 6)
    private BigDecimal totalQuota;

    @Column(name = "used_quota", nullable = false, precision = 24, scale = 6)
    private BigDecimal usedQuota;

    @Column(name = "locked_quota", precision = 24, scale = 6)
    private BigDecimal lockedQuota;

    @Column(name = "available_quota", nullable = false, precision = 24, scale = 6)
    private BigDecimal availableQuota;

    @Column(name = "risk_level", length = 5)
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private QuotaStatus status;

    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "industry_code", length = 50)
    private String industryCode;

    @Column(name = "region_code", length = 50)
    private String regionCode;

    @Column(name = "product_type", length = 50)
    private String productType;

    @Column(name = "create_by", length = 50)
    private String createBy;

    @Column(name = "update_by", length = 50)
    private String updateBy;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private GroupQuota groupQuota;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (usedQuota == null) {
            usedQuota = BigDecimal.ZERO;
        }
        if (lockedQuota == null) {
            lockedQuota = BigDecimal.ZERO;
        }
        if (availableQuota == null) {
            availableQuota = totalQuota;
        }
        if (status == null) {
            status = QuotaStatus.ENABLED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public BigDecimal getUsageRate() {
        if (totalQuota == null || totalQuota.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal used = usedQuota != null ? usedQuota : BigDecimal.ZERO;
        return used.multiply(BigDecimal.valueOf(100))
                .divide(totalQuota, 2, java.math.RoundingMode.HALF_UP);
    }
}
