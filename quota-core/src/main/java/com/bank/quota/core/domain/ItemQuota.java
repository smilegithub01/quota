package com.bank.quota.core.domain;

import com.bank.quota.core.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_quota", indexes = {
    @Index(name = "idx_item_no", columnList = "item_no"),
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_approval_id", columnList = "approval_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemQuota implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_no", nullable = false, unique = true, length = 50)
    private String itemNo;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "approval_id")
    private Long approvalId;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(name = "item_type", length = 50)
    private String itemType;

    @Column(name = "item_quota", nullable = false, precision = 24, scale = 6)
    private BigDecimal itemQuota;

    @Column(name = "used_quota", nullable = false, precision = 24, scale = 6)
    private BigDecimal usedQuota;

    @Column(name = "available_quota", nullable = false, precision = 24, scale = 6)
    private BigDecimal availableQuota;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "create_by", nullable = false, length = 50)
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
            status = ItemStatus.ACTIVE;
        }
        if (usedQuota == null) {
            usedQuota = BigDecimal.ZERO;
        }
        if (availableQuota == null) {
            availableQuota = itemQuota;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
