package com.bank.quota.core.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_occupancy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractOccupancy implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_no", nullable = false, unique = true, length = 50)
    private String contractNo;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "approval_quota_sub_id")
    private Long approvalQuotaSubId;

    @Column(name = "occupancy_amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal occupancyAmount;

    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "CNY";

    @Column(name = "original_amount", precision = 20, scale = 4)
    private BigDecimal originalAmount;

    @Column(name = "cny_equivalent", precision = 20, scale = 4)
    private BigDecimal cnyEquivalent;

    @Column(name = "exchange_rate", precision = 20, scale = 8)
    private BigDecimal exchangeRate;

    @Column(name = "transaction_id", length = 128)
    private String transactionId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OccupancyStatus status = OccupancyStatus.PRE_OCCUPIED;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @Column(name = "create_by", length = 50)
    private String createBy;

    @Column(name = "business_type", length = 50)
    private String businessType;

    @Column(name = "occupancy_purpose", length = 200)
    private String occupancyPurpose;

    @Column(name = "multi_occupancy", columnDefinition = "BIT(1)")
    @Builder.Default
    private Boolean multiOccupancy = false;

    @Column(name = "multi_occupancy_type", length = 50)
    private String multiOccupancyType;

    @Column(name = "parent_occupy_id", length = 50)
    private String parentOccupyId;

    @Column(name = "occupancy_sequence")
    private Integer occupancySequence;

    @Column(name = "time_slot_id")
    private Long timeSlotId;

    @Column(name = "update_by", length = 50)
    private String updateBy;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public enum OccupancyStatus {
        PRE_OCCUPIED,
        OCCUPIED,
        RELEASED,
        CANCELLED,
        EXPIRED
    }
}
