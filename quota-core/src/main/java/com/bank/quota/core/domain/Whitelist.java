package com.bank.quota.core.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "whitelist")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Whitelist implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "whitelist_no", nullable = false, unique = true, length = 50)
    private String whitelistNo;

    @Column(name = "whitelist_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private WhitelistType whitelistType;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "business_type", length = 50)
    private String businessType;

    @Column(name = "exempt_rule", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ExemptRule exemptRule;

    @Column(name = "exempt_amount", precision = 20, scale = 4)
    private BigDecimal exemptAmount;

    @Column(name = "effective_time", nullable = false)
    private LocalDateTime effectiveTime;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private WhitelistStatus status;

    @Column(name = "apply_reason", nullable = false, length = 500)
    private String applyReason;

    @Column(name = "approve_remark", length = 500)
    private String approveRemark;

    @Column(name = "applicant", nullable = false, length = 50)
    private String applicant;

    @Column(name = "approver", length = 50)
    private String approver;

    @Column(name = "apply_time", nullable = false)
    private LocalDateTime applyTime;

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
        if (applyTime == null) {
            applyTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public enum WhitelistType {
        CUSTOMER, BUSINESS, AMOUNT
    }

    public enum ExemptRule {
        FULL, PARTIAL, THRESHOLD
    }

    public enum WhitelistStatus {
        PENDING, ACTIVE, INVALID
    }

    public boolean isEffective() {
        LocalDateTime now = LocalDateTime.now();
        return status == WhitelistStatus.ACTIVE 
            && effectiveTime.isBefore(now) 
            && expiryTime.isAfter(now);
    }
}
