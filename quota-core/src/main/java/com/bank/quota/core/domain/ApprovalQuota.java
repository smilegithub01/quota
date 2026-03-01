package com.bank.quota.core.domain;

import com.bank.quota.core.enums.QuotaStatus;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_quota")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalQuota implements Serializable {
    public static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "customer_quota_sub_id", nullable = false)
    public Long customerQuotaSubId;

    @Column(name = "approval_no", nullable = false, unique = true, length = 50)
    public String approvalNo;

    @Column(name = "approval_type", nullable = false, length = 50)
    public String approvalType;

    @Column(name = "approval_quota", nullable = false, precision = 24, scale = 6)
    public BigDecimal approvalQuota;

    @Column(name = "used_quota", nullable = false, precision = 24, scale = 6)
    public BigDecimal usedQuota;

    @Column(name = "locked_quota", precision = 24, scale = 6)
    public BigDecimal lockedQuota;

    @Column(name = "available_quota", nullable = false, precision = 24, scale = 6)
    public BigDecimal availableQuota;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    public QuotaStatus status;

    @Column(name = "create_time", nullable = false, updatable = false)
    public LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    public LocalDateTime updateTime;

    @Column(name = "create_by", length = 50)
    public String createBy;

    @Column(name = "update_by", length = 50)
    public String updateBy;

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
            availableQuota = approvalQuota;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
