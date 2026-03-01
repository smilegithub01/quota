package com.bank.quota.core.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_quota_sub")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerQuotaSub implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_quota_id", nullable = false)
    private Long customerQuotaId;

    @Column(name = "sub_type", nullable = false, length = 50)
    private String subType;

    @Column(name = "sub_type_name", nullable = false, length = 100)
    private String subTypeName;

    @Column(name = "sub_quota", nullable = false, precision = 20, scale = 4)
    private BigDecimal subQuota;

    @Column(name = "used_quota", nullable = false, precision = 20, scale = 4)
    private BigDecimal usedQuota;

    @Column(name = "available_quota", nullable = false, precision = 20, scale = 4)
    private BigDecimal availableQuota;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private QuotaStatus status;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @Column(name = "create_by", length = 50)
    private String createBy;

    @Column(name = "update_by", length = 50)
    private String updateBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_quota_id", insertable = false, updatable = false)
    private CustomerQuota customerQuota;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (usedQuota == null) {
            usedQuota = BigDecimal.ZERO;
        }
        if (availableQuota == null) {
            availableQuota = subQuota;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public enum QuotaStatus {
        ENABLED, FROZEN, DISABLED
    }

    public static class SubType {
        public static final String GENERAL_EXPOSURE = "GENERAL_EXPOSURE";
        public static final String BOND_UNDERWRITING = "BOND_UNDERWRITING";
        public static final String ACTIVE_INVESTMENT = "ACTIVE_INVESTMENT";
        public static final String R1 = "R1";
        public static final String R2 = "R2";
        public static final String R3 = "R3";
        public static final String R4 = "R4";
        public static final String R5 = "R5";
    }
}
