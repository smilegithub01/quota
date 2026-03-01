package com.bank.quota.core.domain;

import com.bank.quota.core.enums.QuotaStatus;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_quota")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupQuota implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false, unique = true)
    private Long groupId;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "total_quota", nullable = false, precision = 24, scale = 6)
    private BigDecimal totalQuota;

    @Column(name = "used_quota", nullable = false, precision = 24, scale = 6)
    private BigDecimal usedQuota;

    @Column(name = "locked_quota", precision = 24, scale = 6)
    private BigDecimal lockedQuota;

    @Column(name = "available_quota", nullable = false, precision = 24, scale = 6)
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
