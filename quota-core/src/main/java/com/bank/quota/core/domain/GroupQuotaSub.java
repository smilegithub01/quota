package com.bank.quota.core.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_quota_sub")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupQuotaSub implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_quota_id", nullable = false)
    private Long groupQuotaId;

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
    @JoinColumn(name = "group_quota_id", insertable = false, updatable = false)
    private GroupQuota groupQuota;

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
        public static final String EXPOSURE = "EXPOSURE";
        public static final String LOW_RISK = "LOW_RISK";
    }
}
